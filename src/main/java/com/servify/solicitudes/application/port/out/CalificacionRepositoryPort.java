package com.servify.solicitudes.application.port.out;

import com.servify.solicitudes.domain.model.Calificacion;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Es el puerto de salida que necesita el módulo solicitudes para persistir y
 consultar calificaciones asociadas a prestaciones finalizadas.
 El MVP exige que la calificación sea única por solicitud finalizada y esté asociada al prestador correspondiente
 */

/**
 * buscarPorSolicitudId(...) es clave porque el negocio no permite más de una calificación por solicitud.
 * buscarPorPrestadorId(...) sirve para reputación y consultas futuras.
 * buscarPorSolicitanteId(...) sirve para trazabilidad e historial del usuario.
 */

public interface CalificacionRepositoryPort {

    Calificacion guardar(Calificacion calificacion);

    Optional<Calificacion> buscarPorId(UUID calificacionId);

    Optional<Calificacion> buscarPorSolicitudId(UUID solicitudId);

    Optional<Calificacion> buscarPorAsignacionServicioIdYRolCalificador(
            UUID asignacionServicioId,
            RolConfirmante rolCalificador
    );

    List<Calificacion> buscarPorPrestadorId(UUID prestadorId);

    List<Calificacion> buscarPorSolicitanteId(UUID solicitanteId);
}
