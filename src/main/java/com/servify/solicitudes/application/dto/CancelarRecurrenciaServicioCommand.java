package com.servify.solicitudes.application.dto;

import java.util.UUID;

public class CancelarRecurrenciaServicioCommand {

    private UUID solicitudId;
    private UUID usuarioId;
    private String motivo;

    public CancelarRecurrenciaServicioCommand() {
    }

    public CancelarRecurrenciaServicioCommand(UUID solicitudId, UUID usuarioId, String motivo) {
        this.solicitudId = solicitudId;
        this.usuarioId = usuarioId;
        this.motivo = motivo;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getMotivo() {
        return motivo;
    }
}
