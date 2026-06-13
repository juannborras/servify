package com.servify.notificaciones.application.dto;

import com.servify.notificaciones.domain.enumtype.TipoNotificacion;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificacionUsuarioResult {

    private UUID id;
    private UUID usuarioId;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private String referenciaTipo;
    private UUID referenciaId;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;

    private NotificacionUsuarioResult() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public UUID getReferenciaId() {
        return referenciaId;
    }

    public Boolean getLeida() {
        return leida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaLectura() {
        return fechaLectura;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificacionUsuarioResult instance;

        public Builder() {
            this.instance = new NotificacionUsuarioResult();
        }

        public Builder id(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder usuarioId(UUID usuarioId) {
            instance.usuarioId = usuarioId;
            return this;
        }

        public Builder tipo(TipoNotificacion tipo) {
            instance.tipo = tipo;
            return this;
        }

        public Builder titulo(String titulo) {
            instance.titulo = titulo;
            return this;
        }

        public Builder mensaje(String mensaje) {
            instance.mensaje = mensaje;
            return this;
        }

        public Builder referenciaTipo(String referenciaTipo) {
            instance.referenciaTipo = referenciaTipo;
            return this;
        }

        public Builder referenciaId(UUID referenciaId) {
            instance.referenciaId = referenciaId;
            return this;
        }

        public Builder leida(Boolean leida) {
            instance.leida = leida;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            instance.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder fechaLectura(LocalDateTime fechaLectura) {
            instance.fechaLectura = fechaLectura;
            return this;
        }

        public NotificacionUsuarioResult build() {
            return instance;
        }
    }
}
