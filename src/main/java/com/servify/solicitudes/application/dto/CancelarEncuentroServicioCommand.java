package com.servify.solicitudes.application.dto;

import java.util.UUID;

public class CancelarEncuentroServicioCommand {

    private UUID encuentroId;
    private UUID usuarioId;

    public CancelarEncuentroServicioCommand() {
    }

    public CancelarEncuentroServicioCommand(UUID encuentroId, UUID usuarioId) {
        this.encuentroId = encuentroId;
        this.usuarioId = usuarioId;
    }

    public UUID getEncuentroId() {
        return encuentroId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }
}
