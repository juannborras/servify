package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.util.UUID;

/**
 * Es el DTO de entrada del caso de uso mediante el cual una de las partes confirma que el servicio fue efectivamente realizado.
 * Como el sistema exige confirmación explícita de solicitante y prestador, el command tiene que identificar:
 * la solicitud
 * la asignación
 * quién confirma
 * qué rol tiene en esa confirmación
 */

/**
 - solicitudId: ubica la solicitud general
 - asignacionServicioId: asegura que la confirmación quede ligada a la asignación efectiva
 - confirmanteId: identifica al usuario que confirma
 - rolConfirmante: distingue si confirma el solicitante o el prestador
 - observacion: la dejé opcional por si después quieren guardar una nota breve sin tocar el contrato
 */

public class ConfirmarFinalizacionServicioCommand {

    private UUID solicitudId;
    private UUID asignacionServicioId;
    private UUID encuentroServicioId;
    private UUID confirmanteId;
    private RolConfirmante rolConfirmante;
    private String observacion;

    public ConfirmarFinalizacionServicioCommand() {
    }

    public ConfirmarFinalizacionServicioCommand(UUID solicitudId,
                                                UUID asignacionServicioId,
                                                UUID confirmanteId,
                                                RolConfirmante rolConfirmante,
                                                String observacion) {
        this(solicitudId, asignacionServicioId, null, confirmanteId, rolConfirmante, observacion);
    }

    public ConfirmarFinalizacionServicioCommand(UUID solicitudId,
                                                UUID asignacionServicioId,
                                                UUID encuentroServicioId,
                                                UUID confirmanteId,
                                                RolConfirmante rolConfirmante,
                                                String observacion) {
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.encuentroServicioId = encuentroServicioId;
        this.confirmanteId = confirmanteId;
        this.rolConfirmante = rolConfirmante;
        this.observacion = observacion;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public UUID getEncuentroServicioId() {
        return encuentroServicioId;
    }

    public UUID getConfirmanteId() {
        return confirmanteId;
    }

    public RolConfirmante getRolConfirmante() {
        return rolConfirmante;
    }

    public String getObservacion() {
        return observacion;
    }
}
