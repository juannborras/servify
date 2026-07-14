package com.servify.pagos.application.service;

import com.servify.pagos.application.dto.IniciarPagoServicioCommand;
import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.port.in.IniciarPagoServicioUseCase;
import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.MercadoPagoGatewayPort;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.pagos.domain.model.PagoServicio;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class IniciarPagoServicioService implements IniciarPagoServicioUseCase {

    private final PagoServicioRepositoryPort pagoRepository;
    private final MercadoPagoGatewayPort mercadoPagoGateway;
    private final EstadoIntegracionPagoPort estadoIntegracion;
    private final SolicitudServicioRepositoryPort solicitudRepository;
    private final AsignacionServicioRepositoryPort asignacionRepository;
    private final ServicioEncuentroRepositoryPort encuentroRepository;
    private final ServicioRecurrenciaRepositoryPort recurrenciaRepository;

    public IniciarPagoServicioService(PagoServicioRepositoryPort pagoRepository,
                                      MercadoPagoGatewayPort mercadoPagoGateway,
                                      EstadoIntegracionPagoPort estadoIntegracion,
                                      SolicitudServicioRepositoryPort solicitudRepository,
                                      AsignacionServicioRepositoryPort asignacionRepository,
                                      ServicioEncuentroRepositoryPort encuentroRepository) {
        this(pagoRepository, mercadoPagoGateway, estadoIntegracion, solicitudRepository,
                asignacionRepository, encuentroRepository, null);
    }

    public IniciarPagoServicioService(PagoServicioRepositoryPort pagoRepository,
                                      MercadoPagoGatewayPort mercadoPagoGateway,
                                      EstadoIntegracionPagoPort estadoIntegracion,
                                      SolicitudServicioRepositoryPort solicitudRepository,
                                      AsignacionServicioRepositoryPort asignacionRepository,
                                      ServicioEncuentroRepositoryPort encuentroRepository,
                                      ServicioRecurrenciaRepositoryPort recurrenciaRepository) {
        this.pagoRepository = pagoRepository;
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.estadoIntegracion = estadoIntegracion;
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
        this.encuentroRepository = encuentroRepository;
        this.recurrenciaRepository = recurrenciaRepository;
    }

    @Override
    // Si la API externa corta despues de crear la preferencia, se conserva el
    // pago local y su idempotency key; el reintento no puede generar otro cobro.
    @Transactional(noRollbackFor = IllegalStateException.class)
    public PagoServicioResult iniciar(IniciarPagoServicioCommand command) {
        validarCommand(command);
        if (!estadoIntegracion.estaHabilitada()) {
            throw new IllegalStateException("Mercado Pago no esta configurado. Defini SERVIFY_MERCADOPAGO_ACCESS_TOKEN en el backend");
        }

        SolicitudServicio solicitud = solicitudRepository.buscarPorId(command.solicitudId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        ServicioEncuentro encuentroBloqueado = bloquearObjetivoRecurrente(command, solicitud);
        AsignacionServicio asignacion = asignacionRepository.buscarPorIdParaActualizar(command.asignacionServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada"));
        validarParticipantes(command, solicitud, asignacion);
        validarObjetivo(command, solicitud, asignacion, encuentroBloqueado);

        PagoServicio pago = pagoRepository
                // El lock de asignacion ya serializa el checkout. No se bloquea
                // la fila de pago para no invertir pago->asignacion contra el webhook.
                .buscarPorObjetivo(asignacion.getId(), command.encuentroId())
                .orElseGet(() -> PagoServicio.nuevo(
                        solicitud.getId(), asignacion.getId(), command.encuentroId(),
                        solicitud.getSolicitanteId(), obtenerMonto(asignacion, solicitud)));

        // Nunca se crea una segunda preferencia para el mismo objetivo: aun si un
        // intento fue rechazado, se reutiliza la URL para evitar doble cobro.
        if (pago.estaAprobado() || pago.tieneCheckoutVigente()) {
            return PagoServicioResult.desde(pago);
        }
        if (pago.getFechaCreacion() == null) pago.marcarCreacion(java.time.LocalDateTime.now());
        else pago.prepararReintento();
        pagoRepository.guardar(pago);

        MercadoPagoGatewayPort.PreferenciaCreada preferencia = mercadoPagoGateway.crearPreferencia(
                new MercadoPagoGatewayPort.SolicitudPreferencia(
                        pago.getId(), solicitud.getId(), command.encuentroId(), pago.getExternalReference(),
                        tituloPago(solicitud, command.encuentroId()), pago.getMonto(), pago.getMoneda()));
        pago.registrarPreferencia(preferencia.preferenceId(), preferencia.checkoutUrl());
        return PagoServicioResult.desde(pagoRepository.guardar(pago));
    }

    private void validarCommand(IniciarPagoServicioCommand command) {
        if (command == null || command.solicitudId() == null || command.solicitanteId() == null
                || command.asignacionServicioId() == null) {
            throw new IllegalArgumentException("solicitudId, solicitanteId y asignacionServicioId son obligatorios");
        }
    }

    private void validarParticipantes(IniciarPagoServicioCommand command,
                                       SolicitudServicio solicitud,
                                       AsignacionServicio asignacion) {
        if (!Objects.equals(solicitud.getSolicitanteId(), command.solicitanteId())) {
            throw new IllegalArgumentException("Solo el solicitante puede iniciar el pago");
        }
        if (!asignacion.correspondeASolicitud(solicitud.getId())) {
            throw new IllegalArgumentException("La asignacion no corresponde a la solicitud");
        }
        if (!solicitud.estaAsignada() || !asignacion.estaActiva()) {
            throw new IllegalStateException("El servicio debe estar asignado y activo para pagarse");
        }
    }

    private void validarObjetivo(IniciarPagoServicioCommand command,
                                 SolicitudServicio solicitud,
                                 AsignacionServicio asignacion,
                                 ServicioEncuentro encuentroBloqueado) {
        if (!solicitud.esRecurrente()) {
            if (command.encuentroId() != null) {
                throw new IllegalArgumentException("Una solicitud no recurrente se paga por asignacion");
            }
            return;
        }
        if (command.encuentroId() == null) {
            throw new IllegalArgumentException("El pago recurrente requiere encuentroId");
        }
        ServicioEncuentro encuentro = encuentroBloqueado != null ? encuentroBloqueado
                : encuentroRepository.buscarPorIdParaActualizar(command.encuentroId())
                    .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado"));
        if (!Objects.equals(encuentro.getSolicitudId(), solicitud.getId())
                || !Objects.equals(encuentro.getAsignacionServicioId(), asignacion.getId())) {
            throw new IllegalArgumentException("El encuentro no corresponde al servicio indicado");
        }
        if (!encuentro.estaConfirmado()) {
            throw new IllegalStateException("Solo se puede pagar un encuentro confirmado y vigente");
        }
        if (encuentro.getFechaFin().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("El pago se habilita cuando finaliza el encuentro programado");
        }
    }

    private ServicioEncuentro bloquearObjetivoRecurrente(IniciarPagoServicioCommand command,
                                                          SolicitudServicio solicitud) {
        if (!solicitud.esRecurrente() || command.encuentroId() == null) return null;
        ServicioEncuentro referencia = encuentroRepository.buscarPorId(command.encuentroId())
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado"));
        if (referencia.getRecurrenciaServicioId() == null) {
            throw new IllegalArgumentException("El encuentro no pertenece a un programa recurrente");
        }
        if (recurrenciaRepository != null) {
            var recurrencia = recurrenciaRepository.buscarPorIdParaActualizar(referencia.getRecurrenciaServicioId())
                    .orElseThrow(() -> new IllegalStateException("La recurrencia ya no existe"));
            if (!recurrencia.estaActiva()) {
                throw new IllegalStateException("La recurrencia no esta activa");
            }
        }
        return encuentroRepository.buscarPorIdParaActualizar(command.encuentroId())
                .orElseThrow(() -> new IllegalStateException("El encuentro ya no existe"));
    }

    private BigDecimal obtenerMonto(AsignacionServicio asignacion, SolicitudServicio solicitud) {
        BigDecimal acordado = asignacion.getPrecioAcordado();
        BigDecimal monto = acordado != null && acordado.signum() > 0
                ? acordado : solicitud.getPrecioReferencia();
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalStateException("El servicio necesita un precio acordado mayor a cero antes de pagar");
        }
        return monto;
    }

    private String tituloPago(SolicitudServicio solicitud, java.util.UUID encuentroId) {
        String base = solicitud.getDescripcionNecesidad();
        if (base == null || base.isBlank()) base = "Servicio Servify";
        base = base.trim();
        if (base.length() > 90) base = base.substring(0, 90);
        return encuentroId == null ? base : base + " - encuentro";
    }
}
