package com.servify.solicitudes.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProponerEncuentroServicioCommand {

    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID propuestoPorId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String mensaje;

    public ProponerEncuentroServicioCommand() {
    }

    public ProponerEncuentroServicioCommand(UUID solicitudId,
                                            UUID asignacionServicioId,
                                            UUID propuestoPorId,
                                            LocalDateTime fechaInicio,
                                            LocalDateTime fechaFin,
                                            String mensaje) {
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.propuestoPorId = propuestoPorId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.mensaje = mensaje;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
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

    public String getMensaje() {
        return mensaje;
    }
}
