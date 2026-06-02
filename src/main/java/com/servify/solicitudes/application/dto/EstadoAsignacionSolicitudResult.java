package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;

import java.util.List;
import java.util.UUID;

public class EstadoAsignacionSolicitudResult {

    private UUID solicitudId;
    private UUID solicitanteId;
    private EstadoSolicitud estadoSolicitud;
    private AsignacionServicioResult asignacion;
    private List<ContraofertaResult> contraofertasPendientes;
    private List<DistribucionSolicitudResult> distribucionesAceptadas;
    private Integer distribucionesActivas;
    private Boolean confirmadoPorSolicitante;
    private Boolean confirmadoPorPrestador;
    private Boolean finalizacionConfirmada;

    private EstadoAsignacionSolicitudResult() {
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public EstadoSolicitud getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public AsignacionServicioResult getAsignacion() {
        return asignacion;
    }

    public List<ContraofertaResult> getContraofertasPendientes() {
        return contraofertasPendientes;
    }

    public List<DistribucionSolicitudResult> getDistribucionesAceptadas() {
        return distribucionesAceptadas;
    }

    public Integer getDistribucionesActivas() {
        return distribucionesActivas;
    }

    public Boolean getConfirmadoPorSolicitante() {
        return confirmadoPorSolicitante;
    }

    public Boolean getConfirmadoPorPrestador() {
        return confirmadoPorPrestador;
    }

    public Boolean getFinalizacionConfirmada() {
        return finalizacionConfirmada;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final EstadoAsignacionSolicitudResult instance;

        public Builder() {
            this.instance = new EstadoAsignacionSolicitudResult();
        }

        public Builder solicitudId(UUID solicitudId) {
            instance.solicitudId = solicitudId;
            return this;
        }

        public Builder solicitanteId(UUID solicitanteId) {
            instance.solicitanteId = solicitanteId;
            return this;
        }

        public Builder estadoSolicitud(EstadoSolicitud estadoSolicitud) {
            instance.estadoSolicitud = estadoSolicitud;
            return this;
        }

        public Builder asignacion(AsignacionServicioResult asignacion) {
            instance.asignacion = asignacion;
            return this;
        }

        public Builder contraofertasPendientes(List<ContraofertaResult> contraofertasPendientes) {
            instance.contraofertasPendientes = contraofertasPendientes;
            return this;
        }

        public Builder distribucionesAceptadas(List<DistribucionSolicitudResult> distribucionesAceptadas) {
            instance.distribucionesAceptadas = distribucionesAceptadas;
            return this;
        }

        public Builder distribucionesActivas(Integer distribucionesActivas) {
            instance.distribucionesActivas = distribucionesActivas;
            return this;
        }

        public Builder confirmadoPorSolicitante(Boolean confirmadoPorSolicitante) {
            instance.confirmadoPorSolicitante = confirmadoPorSolicitante;
            return this;
        }

        public Builder confirmadoPorPrestador(Boolean confirmadoPorPrestador) {
            instance.confirmadoPorPrestador = confirmadoPorPrestador;
            return this;
        }

        public Builder finalizacionConfirmada(Boolean finalizacionConfirmada) {
            instance.finalizacionConfirmada = finalizacionConfirmada;
            return this;
        }

        public EstadoAsignacionSolicitudResult build() {
            return instance;
        }
    }
}
