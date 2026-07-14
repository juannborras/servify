package com.servify.solicitudes.application.port.out;

import com.servify.solicitudes.domain.model.AsignacionServicio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
   Es el puerto de salida que necesita el módulo solicitudes para persistir y
   consultar asignaciones efectivas de solicitudes a prestadores.
 */

public interface AsignacionServicioRepositoryPort {

    AsignacionServicio guardar(AsignacionServicio asignacionServicio);

    Optional<AsignacionServicio> buscarPorId(UUID asignacionServicioId);

    default Optional<AsignacionServicio> buscarPorIdParaActualizar(UUID asignacionServicioId) {
        return buscarPorId(asignacionServicioId);
    }

    // solo tiene sentido si después la implementación puede resolver esa relación por join con SolicitudServicio,
    // porque AsignacionServicio no tiene solicitanteId directo en el dominio.
    Optional<AsignacionServicio> buscarPorSolicitudId(UUID solicitudId);


    List<AsignacionServicio> buscarPorPrestadorId(UUID prestadorId);

    List<AsignacionServicio> buscarPorSolicitanteId(UUID solicitanteId);
}
