package com.servify.notificaciones.application.dto;

import com.servify.notificaciones.domain.enumtype.TipoNotificacion;

import java.util.UUID;

public class CrearNotificacionUsuarioCommand {

    private final UUID usuarioId;
    private final TipoNotificacion tipo;
    private final String titulo;
    private final String mensaje;
    private final String referenciaTipo;
    private final UUID referenciaId;

    public CrearNotificacionUsuarioCommand(
            UUID usuarioId,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String referenciaTipo,
            UUID referenciaId
    ) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
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
}
