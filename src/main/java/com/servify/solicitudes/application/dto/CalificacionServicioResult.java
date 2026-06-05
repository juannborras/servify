package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.time.LocalDateTime;
import java.util.UUID;

public class CalificacionServicioResult {

    private UUID id;
    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID calificadorId;
    private UUID calificadoId;
    private RolConfirmante rolCalificador;
    private Integer puntaje;
    private LocalDateTime fechaCalificacion;

    private CalificacionServicioResult() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
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

    public LocalDateTime getFechaCalificacion() {
        return fechaCalificacion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final CalificacionServicioResult instance;

        public Builder() {
            this.instance = new CalificacionServicioResult();
        }

        public Builder id(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder solicitudId(UUID solicitudId) {
            instance.solicitudId = solicitudId;
            return this;
        }

        public Builder asignacionServicioId(UUID asignacionServicioId) {
            instance.asignacionServicioId = asignacionServicioId;
            return this;
        }

        public Builder calificadorId(UUID calificadorId) {
            instance.calificadorId = calificadorId;
            return this;
        }

        public Builder calificadoId(UUID calificadoId) {
            instance.calificadoId = calificadoId;
            return this;
        }

        public Builder rolCalificador(RolConfirmante rolCalificador) {
            instance.rolCalificador = rolCalificador;
            return this;
        }

        public Builder puntaje(Integer puntaje) {
            instance.puntaje = puntaje;
            return this;
        }

        public Builder fechaCalificacion(LocalDateTime fechaCalificacion) {
            instance.fechaCalificacion = fechaCalificacion;
            return this;
        }

        public CalificacionServicioResult build() {
            return instance;
        }
    }
}
