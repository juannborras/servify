package com.servify.solicitudes.domain.model;

import com.servify.shared.domain.model.BaseEntity;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ServicioEncuentro extends BaseEntity {

    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID recurrenciaServicioId;
    private UUID propuestoPorId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoEncuentroServicio estado;
    private String mensaje;
    private LocalDateTime fechaResolucion;

    protected ServicioEncuentro() {
    }

    public ServicioEncuentro(UUID id,
                             UUID solicitudId,
                             UUID asignacionServicioId,
                             UUID propuestoPorId,
                             LocalDateTime fechaInicio,
                             LocalDateTime fechaFin,
                             EstadoEncuentroServicio estado,
                             String mensaje,
                             LocalDateTime fechaResolucion) {
        this(id, solicitudId, asignacionServicioId, null, propuestoPorId, fechaInicio, fechaFin,
                estado, mensaje, fechaResolucion);
    }

    public ServicioEncuentro(UUID id,
                             UUID solicitudId,
                             UUID asignacionServicioId,
                             UUID recurrenciaServicioId,
                             UUID propuestoPorId,
                             LocalDateTime fechaInicio,
                             LocalDateTime fechaFin,
                             EstadoEncuentroServicio estado,
                             String mensaje,
                             LocalDateTime fechaResolucion) {
        super(id);
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.recurrenciaServicioId = recurrenciaServicioId;
        this.propuestoPorId = propuestoPorId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.mensaje = mensaje;
        this.fechaResolucion = fechaResolucion;
        validarFechas();
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public UUID getRecurrenciaServicioId() {
        return recurrenciaServicioId;
    }

    public UUID getPropuestoPorId() {
        return propuestoPorId;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public EstadoEncuentroServicio getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public boolean estaPendiente() {
        return estado == EstadoEncuentroServicio.PROPUESTO;
    }

    public boolean estaConfirmado() {
        return estado == EstadoEncuentroServicio.CONFIRMADO;
    }

    public boolean estaCerrado() {
        return estado == EstadoEncuentroServicio.RECHAZADO
                || estado == EstadoEncuentroServicio.CANCELADO
                || estado == EstadoEncuentroServicio.COMPLETADO;
    }

    public boolean fuePropuestoPor(UUID usuarioId) {
        return usuarioId != null && Objects.equals(this.propuestoPorId, usuarioId);
    }

    public void confirmar(LocalDateTime fechaResolucion) {
        if (!estaPendiente()) {
            throw new IllegalStateException("Solo se puede confirmar un encuentro propuesto");
        }
        this.estado = EstadoEncuentroServicio.CONFIRMADO;
        this.fechaResolucion = fechaResolucion != null ? fechaResolucion : LocalDateTime.now();
    }

    public void rechazar(LocalDateTime fechaResolucion) {
        if (!estaPendiente()) {
            throw new IllegalStateException("Solo se puede rechazar un encuentro propuesto");
        }
        this.estado = EstadoEncuentroServicio.RECHAZADO;
        this.fechaResolucion = fechaResolucion != null ? fechaResolucion : LocalDateTime.now();
    }

    public void cancelar(LocalDateTime fechaResolucion) {
        if (estaCerrado()) {
            throw new IllegalStateException("El encuentro ya esta cerrado");
        }
        this.estado = EstadoEncuentroServicio.CANCELADO;
        this.fechaResolucion = fechaResolucion != null ? fechaResolucion : LocalDateTime.now();
    }

    public void completar(LocalDateTime fechaResolucion) {
        if (!estaConfirmado()) {
            throw new IllegalStateException("Solo se puede completar un encuentro confirmado");
        }
        this.estado = EstadoEncuentroServicio.COMPLETADO;
        this.fechaResolucion = fechaResolucion != null ? fechaResolucion : LocalDateTime.now();
    }

    private void validarFechas() {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas del encuentro son obligatorias");
        }
        if (!fechaInicio.isBefore(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }
}
