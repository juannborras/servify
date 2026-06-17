package com.servify.autenticacion.domain.model;

import com.servify.shared.domain.model.BaseEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetToken extends BaseEntity {

    private UUID usuarioId;
    private UUID credencialAccesoId;
    private String email;
    private String tokenHash;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaUso;
    private Boolean utilizado;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(UUID id,
                              UUID usuarioId,
                              UUID credencialAccesoId,
                              String email,
                              String tokenHash,
                              LocalDateTime fechaCreacion,
                              LocalDateTime fechaExpiracion,
                              LocalDateTime fechaUso,
                              Boolean utilizado) {
        super(id);
        this.usuarioId = usuarioId;
        this.credencialAccesoId = credencialAccesoId;
        this.email = normalizarEmail(email);
        this.tokenHash = validarTexto(tokenHash, "El hash del token es obligatorio");
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaExpiracion = validarFechaExpiracion(fechaExpiracion);
        this.fechaUso = fechaUso;
        this.utilizado = Boolean.TRUE.equals(utilizado);
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getCredencialAccesoId() {
        return credencialAccesoId;
    }

    public String getEmail() {
        return email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public Boolean getUtilizado() {
        return utilizado;
    }

    public boolean estaVigente(LocalDateTime ahora) {
        LocalDateTime referencia = ahora != null ? ahora : LocalDateTime.now();
        return !Boolean.TRUE.equals(utilizado)
                && fechaExpiracion != null
                && referencia.isBefore(fechaExpiracion);
    }

    public void marcarUsado(LocalDateTime fechaUso) {
        if (Boolean.TRUE.equals(utilizado)) {
            throw new IllegalStateException("El token ya fue utilizado");
        }
        this.fechaUso = fechaUso != null ? fechaUso : LocalDateTime.now();
        this.utilizado = true;
    }

    public void invalidar(LocalDateTime fechaUso) {
        if (Boolean.TRUE.equals(utilizado)) {
            return;
        }
        this.fechaUso = fechaUso != null ? fechaUso : LocalDateTime.now();
        this.utilizado = true;
    }

    private String normalizarEmail(String email) {
        String normalizado = validarTexto(email, "El email es obligatorio").toLowerCase();
        if (!normalizado.contains("@") || normalizado.startsWith("@") || normalizado.endsWith("@")) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
        return normalizado;
    }

    private LocalDateTime validarFechaExpiracion(LocalDateTime fechaExpiracion) {
        if (fechaExpiracion == null) {
            throw new IllegalArgumentException("La fecha de expiracion es obligatoria");
        }
        return fechaExpiracion;
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
