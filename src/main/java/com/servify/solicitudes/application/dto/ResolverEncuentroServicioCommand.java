package com.servify.solicitudes.application.dto;

import java.util.UUID;

public class ResolverEncuentroServicioCommand {

    private UUID encuentroId;
    private UUID usuarioId;
    private TipoDecisionSolicitud decision;

    public ResolverEncuentroServicioCommand() {
    }

    public ResolverEncuentroServicioCommand(UUID encuentroId,
                                            UUID usuarioId,
                                            TipoDecisionSolicitud decision) {
        this.encuentroId = encuentroId;
        this.usuarioId = usuarioId;
        this.decision = decision;
    }

    public UUID getEncuentroId() {
        return encuentroId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public TipoDecisionSolicitud getDecision() {
        return decision;
    }
}
