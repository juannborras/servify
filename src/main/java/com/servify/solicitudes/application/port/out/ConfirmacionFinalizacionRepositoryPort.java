package com.servify.solicitudes.application.port.out;

import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfirmacionFinalizacionRepositoryPort {

    ConfirmacionFinalizacion guardar(ConfirmacionFinalizacion confirmacionFinalizacion);

    Optional<ConfirmacionFinalizacion> buscarPorId(UUID confirmacionFinalizacionId);

    List<ConfirmacionFinalizacion> buscarPorSolicitudId(UUID solicitudId);

    List<ConfirmacionFinalizacion> buscarPorAsignacionServicioId(UUID asignacionServicioId);

    List<ConfirmacionFinalizacion> buscarPorEncuentroServicioId(UUID encuentroServicioId);

    Optional<ConfirmacionFinalizacion> buscarPorAsignacionServicioIdYRolConfirmante(
            UUID asignacionServicioId,
            com.servify.solicitudes.domain.enumtype.RolConfirmante rolConfirmante
    );

    Optional<ConfirmacionFinalizacion> buscarPorEncuentroServicioIdYRolConfirmante(
            UUID encuentroServicioId,
            com.servify.solicitudes.domain.enumtype.RolConfirmante rolConfirmante
    );

    List<ConfirmacionFinalizacion> buscarPorConfirmanteId(UUID confirmanteId);
}
