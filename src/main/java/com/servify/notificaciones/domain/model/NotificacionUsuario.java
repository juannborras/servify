package com.servify.notificaciones.domain.model;

import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.shared.domain.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificacionUsuario extends BaseEntity {

    private UUID usuarioId;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private String referenciaTipo;
    private UUID referenciaId;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;

    protected NotificacionUsuario() {
    }

    public NotificacionUsuario(
            UUID id,
            UUID usuarioId,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String referenciaTipo,
            UUID referenciaId,
            Boolean leida,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaLectura
    ) {
        super(id);
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
        this.leida = leida != null && leida;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaLectura = fechaLectura;
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

    public void marcarLeida(LocalDateTime fechaLectura) {
        this.leida = true;
        this.fechaLectura = fechaLectura != null ? fechaLectura : LocalDateTime.now();
    }
}
