package com.servify.chat.application.dto;

import java.util.UUID;

public class EnviarMensajeChatCommand {

    private final UUID solicitudId;
    private final UUID prestadorId;
    private final UUID remitenteId;
    private final String contenido;

    public EnviarMensajeChatCommand(UUID solicitudId, UUID prestadorId, UUID remitenteId, String contenido) {
        this.solicitudId = solicitudId;
        this.prestadorId = prestadorId;
        this.remitenteId = remitenteId;
        this.contenido = contenido;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getPrestadorId() {
        return prestadorId;
    }

    public UUID getRemitenteId() {
        return remitenteId;
    }

    public String getContenido() {
        return contenido;
    }
}
