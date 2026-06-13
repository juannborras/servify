package com.servify.autenticacion.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenClaims {

    private final UUID usuarioId;
    private final String emailAcceso;
    private final LocalDateTime fechaEmision;
    private final LocalDateTime fechaExpiracion;

    public TokenClaims(UUID usuarioId, String emailAcceso, LocalDateTime fechaEmision, LocalDateTime fechaExpiracion) {
        this.usuarioId = usuarioId;
        this.emailAcceso = emailAcceso;
        this.fechaEmision = fechaEmision;
        this.fechaExpiracion = fechaExpiracion;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getEmailAcceso() {
        return emailAcceso;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }
}
