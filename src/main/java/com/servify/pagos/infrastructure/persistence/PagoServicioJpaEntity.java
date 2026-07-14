package com.servify.pagos.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pago_servicio")
class PagoServicioJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "asignacion_servicio_id", nullable = false) private Long asignacionServicioId;
    @Column(name = "encuentro_servicio_id") private UUID encuentroServicioId;
    @Column(name = "solicitante_id", nullable = false) private Long solicitanteId;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal monto;
    @Column(nullable = false, length = 3) private String moneda;
    @Column(nullable = false, length = 20) private String estado;
    @Column(name = "external_reference", nullable = false, unique = true, length = 100) private String externalReference;
    @Column(name = "mercadopago_preference_id", unique = true, length = 150) private String mercadoPagoPreferenceId;
    @Column(name = "checkout_url", length = 1000) private String checkoutUrl;
    @Column(name = "mercadopago_payment_id", unique = true, length = 100) private String mercadoPagoPaymentId;
    @Column(name = "aprobado_en") private LocalDateTime aprobadoEn;
    @Column(name = "error_detalle", length = 500) private String errorDetalle;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected PagoServicioJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }
    public Long getAsignacionServicioId() { return asignacionServicioId; }
    public void setAsignacionServicioId(Long asignacionServicioId) { this.asignacionServicioId = asignacionServicioId; }
    public UUID getEncuentroServicioId() { return encuentroServicioId; }
    public void setEncuentroServicioId(UUID encuentroServicioId) { this.encuentroServicioId = encuentroServicioId; }
    public Long getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(Long solicitanteId) { this.solicitanteId = solicitanteId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public String getMercadoPagoPreferenceId() { return mercadoPagoPreferenceId; }
    public void setMercadoPagoPreferenceId(String value) { this.mercadoPagoPreferenceId = value; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getMercadoPagoPaymentId() { return mercadoPagoPaymentId; }
    public void setMercadoPagoPaymentId(String value) { this.mercadoPagoPaymentId = value; }
    public LocalDateTime getAprobadoEn() { return aprobadoEn; }
    public void setAprobadoEn(LocalDateTime aprobadoEn) { this.aprobadoEn = aprobadoEn; }
    public String getErrorDetalle() { return errorDetalle; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
