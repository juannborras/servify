package com.servify.pagos.domain.model;

import com.servify.pagos.domain.enumtype.EstadoPagoServicio;
import com.servify.shared.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Pago de una asignacion unica o de un encuentro concreto de una recurrencia. */
public class PagoServicio extends BaseEntity {

    private final UUID solicitudId;
    private final UUID asignacionServicioId;
    private final UUID encuentroServicioId;
    private final UUID solicitanteId;
    private final BigDecimal monto;
    private final String moneda;
    private final String externalReference;
    private EstadoPagoServicio estado;
    private String mercadoPagoPreferenceId;
    private String checkoutUrl;
    private String mercadoPagoPaymentId;
    private LocalDateTime aprobadoEn;
    private String errorDetalle;

    public PagoServicio(UUID id,
                        UUID solicitudId,
                        UUID asignacionServicioId,
                        UUID encuentroServicioId,
                        UUID solicitanteId,
                        BigDecimal monto,
                        String moneda,
                        EstadoPagoServicio estado,
                        String externalReference,
                        String mercadoPagoPreferenceId,
                        String checkoutUrl,
                        String mercadoPagoPaymentId,
                        LocalDateTime aprobadoEn,
                        String errorDetalle) {
        super(id);
        this.solicitudId = Objects.requireNonNull(solicitudId, "solicitudId no puede ser nulo");
        this.asignacionServicioId = Objects.requireNonNull(asignacionServicioId, "asignacionServicioId no puede ser nulo");
        this.encuentroServicioId = encuentroServicioId;
        this.solicitanteId = Objects.requireNonNull(solicitanteId, "solicitanteId no puede ser nulo");
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        this.monto = monto.setScale(2, java.math.RoundingMode.HALF_UP);
        this.moneda = moneda == null ? "ARS" : moneda.toUpperCase();
        if (!"ARS".equals(this.moneda)) {
            throw new IllegalArgumentException("Servify Argentina solo admite pagos en ARS");
        }
        this.estado = estado == null ? EstadoPagoServicio.PENDIENTE : estado;
        this.externalReference = Objects.requireNonNull(externalReference, "externalReference no puede ser nulo");
        this.mercadoPagoPreferenceId = mercadoPagoPreferenceId;
        this.checkoutUrl = checkoutUrl;
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
        this.aprobadoEn = aprobadoEn;
        this.errorDetalle = errorDetalle;
    }

    public static PagoServicio nuevo(UUID solicitudId,
                                     UUID asignacionServicioId,
                                     UUID encuentroServicioId,
                                     UUID solicitanteId,
                                     BigDecimal monto) {
        String objetivo = asignacionServicioId + ":"
                + (encuentroServicioId == null ? "asignacion" : encuentroServicioId);
        // Deterministico por objetivo: conserva external_reference e idempotency
        // key incluso ante un timeout seguido de rollback/reintento.
        UUID id = UUID.nameUUIDFromBytes(("servify-pago:" + objetivo).getBytes(StandardCharsets.UTF_8));
        return new PagoServicio(id, solicitudId, asignacionServicioId, encuentroServicioId,
                solicitanteId, monto, "ARS", EstadoPagoServicio.PENDIENTE,
                "servify-pago-" + id, null, null, null, null, null);
    }

    public UUID getSolicitudId() { return solicitudId; }
    public UUID getAsignacionServicioId() { return asignacionServicioId; }
    public UUID getEncuentroServicioId() { return encuentroServicioId; }
    public UUID getSolicitanteId() { return solicitanteId; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public EstadoPagoServicio getEstado() { return estado; }
    public String getExternalReference() { return externalReference; }
    public String getMercadoPagoPreferenceId() { return mercadoPagoPreferenceId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public String getMercadoPagoPaymentId() { return mercadoPagoPaymentId; }
    public LocalDateTime getAprobadoEn() { return aprobadoEn; }
    public String getErrorDetalle() { return errorDetalle; }

    public boolean estaAprobado() { return estado == EstadoPagoServicio.APROBADO; }

    public boolean tieneCheckoutVigente() {
        return checkoutUrl != null && !checkoutUrl.isBlank();
    }

    public boolean correspondeA(UUID solicitudId, UUID asignacionId, UUID encuentroId) {
        return Objects.equals(this.solicitudId, solicitudId)
                && Objects.equals(this.asignacionServicioId, asignacionId)
                && Objects.equals(this.encuentroServicioId, encuentroId);
    }

    public void registrarPreferencia(String preferenceId, String checkoutUrl) {
        if (preferenceId == null || preferenceId.isBlank() || checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalArgumentException("Mercado Pago no devolvio una preferencia valida");
        }
        this.mercadoPagoPreferenceId = preferenceId;
        this.checkoutUrl = checkoutUrl;
        this.estado = EstadoPagoServicio.PENDIENTE;
        this.errorDetalle = null;
        marcarModificacion(LocalDateTime.now());
    }

    public void prepararReintento() {
        if (estaAprobado()) {
            throw new IllegalStateException("Un pago aprobado no puede reiniciarse");
        }
        this.estado = EstadoPagoServicio.PENDIENTE;
        if (tieneCheckoutVigente()) {
            throw new IllegalStateException("La preferencia existente debe reutilizarse para evitar cobros duplicados");
        }
        this.mercadoPagoPaymentId = null;
        this.errorDetalle = null;
        marcarModificacion(LocalDateTime.now());
    }

    public void aprobar(String paymentId, LocalDateTime fecha) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId no puede ser vacio");
        }
        this.estado = EstadoPagoServicio.APROBADO;
        this.mercadoPagoPaymentId = paymentId;
        this.aprobadoEn = fecha == null ? LocalDateTime.now() : fecha;
        this.errorDetalle = null;
        marcarModificacion(LocalDateTime.now());
    }

    public void actualizarEstadoExterno(EstadoPagoServicio estado, String paymentId, String detalle) {
        if (estado == null || estado == EstadoPagoServicio.APROBADO) {
            throw new IllegalArgumentException("Use aprobar para registrar un pago acreditado");
        }
        // Un reembolso o contracargo posterior debe conservarse como verdad
        // financiera aun si el servicio ya habia quedado confirmado.
        this.estado = estado;
        if (paymentId != null && !paymentId.isBlank()) this.mercadoPagoPaymentId = paymentId;
        this.errorDetalle = detalle;
        marcarModificacion(LocalDateTime.now());
    }
}
