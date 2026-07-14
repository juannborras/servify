package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServicioEncuentroResult {

    private UUID id;
    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID recurrenciaServicioId;
    private UUID propuestoPorId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoEncuentroServicio estado;
    private String mensaje;
    private LocalDateTime fechaResolucion;

    public ServicioEncuentroResult() {
    }

    public ServicioEncuentroResult(UUID id,
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

    public ServicioEncuentroResult(UUID id,
                                   UUID solicitudId,
                                   UUID asignacionServicioId,
                                   UUID recurrenciaServicioId,
                                   UUID propuestoPorId,
                                   LocalDateTime fechaInicio,
                                   LocalDateTime fechaFin,
                                   EstadoEncuentroServicio estado,
                                   String mensaje,
                                   LocalDateTime fechaResolucion) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.recurrenciaServicioId = recurrenciaServicioId;
        this.propuestoPorId = propuestoPorId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.mensaje = mensaje;
        this.fechaResolucion = fechaResolucion;
    }

    public UUID getId() {
        return id;
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
}
