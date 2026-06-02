package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.EstadoDistribucion;

import java.time.LocalDateTime;
import java.util.UUID;

public class DistribucionSolicitudResult {

    private UUID id;
    private UUID solicitudId;
    private UUID publicacionServicioId;
    private UUID prestadorId;
    private EstadoDistribucion estado;
    private Integer rondaDistribucion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaExpiracion;

    private DistribucionSolicitudResult() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getPublicacionServicioId() {
        return publicacionServicioId;
    }

    public UUID getPrestadorId() {
        return prestadorId;
    }

    public EstadoDistribucion getEstado() {
        return estado;
    }

    public Integer getRondaDistribucion() {
        return rondaDistribucion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final DistribucionSolicitudResult instance;

        public Builder() {
            this.instance = new DistribucionSolicitudResult();
        }

        public Builder id(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder solicitudId(UUID solicitudId) {
            instance.solicitudId = solicitudId;
            return this;
        }

        public Builder publicacionServicioId(UUID publicacionServicioId) {
            instance.publicacionServicioId = publicacionServicioId;
            return this;
        }

        public Builder prestadorId(UUID prestadorId) {
            instance.prestadorId = prestadorId;
            return this;
        }

        public Builder estado(EstadoDistribucion estado) {
            instance.estado = estado;
            return this;
        }

        public Builder rondaDistribucion(Integer rondaDistribucion) {
            instance.rondaDistribucion = rondaDistribucion;
            return this;
        }

        public Builder fechaEnvio(LocalDateTime fechaEnvio) {
            instance.fechaEnvio = fechaEnvio;
            return this;
        }

        public Builder fechaRespuesta(LocalDateTime fechaRespuesta) {
            instance.fechaRespuesta = fechaRespuesta;
            return this;
        }

        public Builder fechaExpiracion(LocalDateTime fechaExpiracion) {
            instance.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public DistribucionSolicitudResult build() {
            return instance;
        }
    }
}
