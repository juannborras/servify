package com.servify.chat.domain.model;

import com.servify.shared.domain.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class MensajeChat extends BaseEntity {

    private UUID solicitudId;
    private UUID solicitanteId;
    private UUID prestadorId;
    private UUID remitenteId;
    private String contenido;
    private LocalDateTime fechaEnvio;

    protected MensajeChat() {
    }

    public MensajeChat(
            UUID id,
            UUID solicitudId,
            UUID solicitanteId,
            UUID prestadorId,
            UUID remitenteId,
            String contenido,
            LocalDateTime fechaEnvio
    ) {
        super(id);
        this.solicitudId = solicitudId;
        this.solicitanteId = solicitanteId;
        this.prestadorId = prestadorId;
        this.remitenteId = remitenteId;
        this.contenido = normalizarContenido(contenido);
        this.fechaEnvio = fechaEnvio;
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

    private String normalizarContenido(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacio");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > 1200) {
            throw new IllegalArgumentException("El mensaje no puede exceder 1200 caracteres");
        }
        return normalizado;
    }
}
