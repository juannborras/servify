package com.servify.solicitudes.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.servify.shared.domain.model.BaseEntity;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;

//La dejé minimalista porque en el MVP no hay reseña textual, solo estrellas.
//Más adelante, si agregan comentarios o feedback escrito, se puede extender con algo como:
//String comentario
//Boolean visible
//LocalDateTime fechaEdicion

public class Calificacion extends BaseEntity {

    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID solicitanteId;
    private UUID prestadorId;
    private UUID calificadorId;
    private UUID calificadoId;
    private RolConfirmante rolCalificador;
    private Integer puntaje;
    private String comentario;
    private LocalDateTime fechaCalificacion;

    protected Calificacion() {
    }

    public Calificacion(UUID id,
                        UUID solicitudId,
                        UUID asignacionServicioId,
                        UUID solicitanteId,
                        UUID prestadorId,
                        Integer puntaje,
                        LocalDateTime fechaCalificacion) {
        this(id, solicitudId, asignacionServicioId, solicitanteId, prestadorId,
                solicitanteId, prestadorId, RolConfirmante.SOLICITANTE, puntaje, null, fechaCalificacion);
    }

    public Calificacion(UUID id,
                        UUID solicitudId,
                        UUID asignacionServicioId,
                        UUID solicitanteId,
                        UUID prestadorId,
                        UUID calificadorId,
                        UUID calificadoId,
                        RolConfirmante rolCalificador,
                        Integer puntaje,
                        LocalDateTime fechaCalificacion) {
        this(id, solicitudId, asignacionServicioId, solicitanteId, prestadorId,
                calificadorId, calificadoId, rolCalificador, puntaje, null, fechaCalificacion);
    }

    public Calificacion(UUID id,
                        UUID solicitudId,
                        UUID asignacionServicioId,
                        UUID solicitanteId,
                        UUID prestadorId,
                        UUID calificadorId,
                        UUID calificadoId,
                        RolConfirmante rolCalificador,
                        Integer puntaje,
                        String comentario,
                        LocalDateTime fechaCalificacion) {
        super(id);
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.solicitanteId = solicitanteId;
        this.prestadorId = prestadorId;
        this.calificadorId = calificadorId;
        this.calificadoId = calificadoId;
        this.rolCalificador = rolCalificador;
        this.puntaje = puntaje;
        this.comentario = normalizarComentario(comentario);
        this.fechaCalificacion = fechaCalificacion;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public UUID getPrestadorId() {
        return prestadorId;
    }

    public UUID getCalificadorId() {
        return calificadorId;
    }

    public UUID getCalificadoId() {
        return calificadoId;
    }

    public RolConfirmante getRolCalificador() {
        return rolCalificador;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFechaCalificacion() {
        return fechaCalificacion;
    }

    public boolean esPuntajeValido() {
        return this.puntaje != null && this.puntaje >= 1 && this.puntaje <= 5;
    }

    public boolean perteneceASolicitud(UUID solicitudId) {
        if (solicitudId == null) {
            return false;
        }
        return this.solicitudId.equals(solicitudId);
    }

    public boolean perteneceAPrestador(UUID prestadorId) {
        if (prestadorId == null) {
            return false;
        }
        return prestadorId.equals(this.calificadoId);
    }

    public boolean fueEmitidaPor(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return usuarioId.equals(this.calificadorId);
    }

    public void actualizarPuntaje(Integer puntaje) {
        if (puntaje == null || puntaje < 1 || puntaje > 5) {
            throw new IllegalArgumentException("El puntaje debe ser un valor entre 1 y 5");
        }
        this.puntaje = puntaje;
    }

    private String normalizarComentario(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String normalizado = valor.trim();
        if (normalizado.length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder 500 caracteres");
        }
        return normalizado;
    }
}
