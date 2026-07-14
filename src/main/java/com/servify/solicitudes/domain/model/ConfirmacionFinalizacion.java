package com.servify.solicitudes.domain.model;

import com.servify.shared.domain.model.BaseEntity;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.time.LocalDateTime;
import java.util.UUID;

//La pensé como una entidad por confirmación.
//Eso deja el flujo más claro:
//la asignación existe
//el solicitante confirma
//el prestador confirma
//cuando ambas confirmaciones están hechas, la solicitud/asignación puede pasar a finalizada

public class ConfirmacionFinalizacion extends BaseEntity {

    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID encuentroServicioId;
    private UUID confirmanteId;
    private RolConfirmante rolConfirmante;
    private Boolean confirmada;
    private LocalDateTime fechaConfirmacion;
    private String observacion;

    protected ConfirmacionFinalizacion() {
    }

    public ConfirmacionFinalizacion(UUID id,
                                    UUID solicitudId,
                                    UUID asignacionServicioId,
                                    UUID confirmanteId,
                                    RolConfirmante rolConfirmante,
                                    Boolean confirmada,
                                    LocalDateTime fechaConfirmacion,
                                    String observacion) {
        this(id, solicitudId, asignacionServicioId, null, confirmanteId, rolConfirmante,
                confirmada, fechaConfirmacion, observacion);
    }

    public ConfirmacionFinalizacion(UUID id,
                                    UUID solicitudId,
                                    UUID asignacionServicioId,
                                    UUID encuentroServicioId,
                                    UUID confirmanteId,
                                    RolConfirmante rolConfirmante,
                                    Boolean confirmada,
                                    LocalDateTime fechaConfirmacion,
                                    String observacion) {
        super(id);
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.encuentroServicioId = encuentroServicioId;
        this.confirmanteId = confirmanteId;
        this.rolConfirmante = rolConfirmante;
        this.confirmada = confirmada;
        this.fechaConfirmacion = fechaConfirmacion;
        this.observacion = observacion;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public UUID getEncuentroServicioId() {
        return encuentroServicioId;
    }

    public UUID getConfirmanteId() {
        return confirmanteId;
    }

    public RolConfirmante getRolConfirmante() {
        return rolConfirmante;
    }

    public Boolean getConfirmada() {
        return confirmada;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public boolean estaConfirmada() {
        return this.confirmada != null && this.confirmada;
    }

    public boolean esDelSolicitante() {
        return this.rolConfirmante == RolConfirmante.SOLICITANTE;
    }

    public boolean esDelPrestador() {
        return this.rolConfirmante == RolConfirmante.PRESTADOR;
    }

    public boolean perteneceAUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return this.confirmanteId.equals(usuarioId);
    }

    public boolean correspondeASolicitud(UUID solicitudId) {
        if (solicitudId == null) {
            return false;
        }
        return this.solicitudId.equals(solicitudId);
    }

    public boolean correspondeAAsignacion(UUID asignacionServicioId) {
        if (asignacionServicioId == null) {
            return false;
        }
        return this.asignacionServicioId.equals(asignacionServicioId);
    }

    public void confirmar(LocalDateTime fechaConfirmacion) {
        if (fechaConfirmacion == null) {
            throw new IllegalArgumentException("La fecha de confirmación no puede ser nula");
        }
        this.confirmada = true;
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public void actualizarObservacion(String observacion) {
        if (observacion != null && observacion.length() > 500) {
            throw new IllegalArgumentException("La observación no puede exceder 500 caracteres");
        }
        this.observacion = observacion;
    }
}
