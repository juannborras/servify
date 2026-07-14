package com.servify.pagos.application.service;

import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.dto.SincronizarPagoServicioCommand;
import com.servify.pagos.application.port.in.SincronizarPagoServicioUseCase;
import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.MercadoPagoGatewayPort;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.pagos.domain.enumtype.EstadoPagoServicio;
import com.servify.pagos.domain.model.PagoServicio;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public class SincronizarPagoServicioService implements SincronizarPagoServicioUseCase {

    private final PagoServicioRepositoryPort pagoRepository;
    private final MercadoPagoGatewayPort mercadoPagoGateway;
    private final EstadoIntegracionPagoPort estadoIntegracion;
    private final ConfirmarFinalizacionServicioUseCase confirmarFinalizacionUseCase;
    private final ConfirmacionFinalizacionRepositoryPort confirmacionRepository;

    public SincronizarPagoServicioService(PagoServicioRepositoryPort pagoRepository,
                                          MercadoPagoGatewayPort mercadoPagoGateway,
                                          EstadoIntegracionPagoPort estadoIntegracion,
                                          ConfirmarFinalizacionServicioUseCase confirmarFinalizacionUseCase,
                                          ConfirmacionFinalizacionRepositoryPort confirmacionRepository) {
        this.pagoRepository = pagoRepository;
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.estadoIntegracion = estadoIntegracion;
        this.confirmarFinalizacionUseCase = confirmarFinalizacionUseCase;
        this.confirmacionRepository = confirmacionRepository;
    }

    @Override
    @Transactional
    public PagoServicioResult sincronizar(SincronizarPagoServicioCommand command) {
        if (command == null || command.pagoId() == null || command.solicitanteId() == null) {
            throw new IllegalArgumentException("pagoId y solicitanteId son obligatorios");
        }
        exigirIntegracion();
        PagoServicio pago = pagoRepository.buscarPorIdParaActualizar(command.pagoId())
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
        if (!pago.getSolicitanteId().equals(command.solicitanteId())) {
            throw new IllegalArgumentException("Solo el solicitante puede sincronizar este pago");
        }
        String paymentId = command.mercadoPagoPaymentId() != null && !command.mercadoPagoPaymentId().isBlank()
                ? command.mercadoPagoPaymentId().trim()
                : null;
        Optional<MercadoPagoGatewayPort.PagoExterno> externo = paymentId != null && !paymentId.isBlank()
                ? Optional.of(mercadoPagoGateway.obtenerPago(paymentId))
                : mercadoPagoGateway.buscarPagoPorExternalReference(pago.getExternalReference());
        if (externo.isEmpty()) {
            return PagoServicioResult.desde(pago);
        }
        aplicarEstadoVerificado(pago, externo.get());
        return PagoServicioResult.desde(pago);
    }

    @Override
    @Transactional
    public void sincronizarWebhook(String mercadoPagoPaymentId) {
        if (mercadoPagoPaymentId == null || mercadoPagoPaymentId.isBlank()) return;
        exigirIntegracion();
        MercadoPagoGatewayPort.PagoExterno externo = mercadoPagoGateway.obtenerPago(mercadoPagoPaymentId.trim());
        if (externo.externalReference() == null || externo.externalReference().isBlank()) return;
        pagoRepository.buscarPorExternalReferenceParaActualizar(externo.externalReference())
                .ifPresent(pago -> aplicarEstadoVerificado(pago, externo));
    }

    private void aplicarEstadoVerificado(PagoServicio pago, MercadoPagoGatewayPort.PagoExterno externo) {
        validarIntegridad(pago, externo);
        EstadoPagoServicio estado = mapearEstado(externo.estado());
        if (estado == EstadoPagoServicio.APROBADO) {
            if (!pago.estaAprobado()) {
                pago.aprobar(externo.paymentId(),
                        externo.fechaAprobacion() == null ? LocalDateTime.now() : externo.fechaAprobacion());
                pagoRepository.guardar(pago);
            }
            confirmarSolicitanteSiCorresponde(pago);
            return;
        }
        pago.actualizarEstadoExterno(estado, externo.paymentId(), externo.detalleEstado());
        pagoRepository.guardar(pago);
    }

    private void validarIntegridad(PagoServicio pago, MercadoPagoGatewayPort.PagoExterno externo) {
        if (!pago.getExternalReference().equals(externo.externalReference())) {
            throw new IllegalStateException("El pago informado por Mercado Pago no corresponde a esta operacion");
        }
        if (externo.monto() == null || pago.getMonto().compareTo(externo.monto()) != 0) {
            throw new IllegalStateException("El monto acreditado por Mercado Pago no coincide con el precio acordado");
        }
        if (!pago.getMoneda().equalsIgnoreCase(externo.moneda())) {
            throw new IllegalStateException("La moneda acreditada por Mercado Pago no coincide; se esperaba ARS");
        }
    }

    private EstadoPagoServicio mapearEstado(String value) {
        String estado = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (estado) {
            case "approved" -> EstadoPagoServicio.APROBADO;
            case "pending", "in_process", "in_mediation", "authorized" -> EstadoPagoServicio.PENDIENTE;
            case "rejected" -> EstadoPagoServicio.RECHAZADO;
            case "cancelled", "refunded", "charged_back" -> EstadoPagoServicio.CANCELADO;
            default -> EstadoPagoServicio.ERROR;
        };
    }

    private void confirmarSolicitanteSiCorresponde(PagoServicio pago) {
        boolean confirmada = pago.getEncuentroServicioId() == null
                ? confirmacionRepository.buscarPorAsignacionServicioIdYRolConfirmante(
                        pago.getAsignacionServicioId(), RolConfirmante.SOLICITANTE).isPresent()
                : confirmacionRepository.buscarPorEncuentroServicioIdYRolConfirmante(
                        pago.getEncuentroServicioId(), RolConfirmante.SOLICITANTE).isPresent();
        if (confirmada) return;
        confirmarFinalizacionUseCase.confirmar(new ConfirmarFinalizacionServicioCommand(
                pago.getSolicitudId(), pago.getAsignacionServicioId(), pago.getEncuentroServicioId(),
                pago.getSolicitanteId(), RolConfirmante.SOLICITANTE,
                "Pago aprobado verificado por Mercado Pago"));
    }

    private void exigirIntegracion() {
        if (!estadoIntegracion.estaHabilitada()) {
            throw new IllegalStateException("Mercado Pago no esta configurado. Defini SERVIFY_MERCADOPAGO_ACCESS_TOKEN en el backend");
        }
    }
}
