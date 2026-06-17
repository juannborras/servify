package com.servify.chat.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensajeChatResult {

    private final UUID id;
    private final UUID solicitudId;
    private final UUID solicitanteId;
    private final UUID prestadorId;
    private final UUID remitenteId;
    private final String contenido;
    private final LocalDateTime fechaEnvio;

    public MensajeChatResult(
            UUID id,
            UUID solicitudId,
            UUID solicitanteId,
            UUID prestadorId,
            UUID remitenteId,
            String contenido,
            LocalDateTime fechaEnvio
    ) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.solicitanteId = solicitanteId;
        this.prestadorId = prestadorId;
        this.remitenteId = remitenteId;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
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

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
