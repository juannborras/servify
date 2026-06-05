package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.CalificacionServicioResult;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.util.Optional;
import java.util.UUID;

public interface ConsultarCalificacionServicioUseCase {

    Optional<CalificacionServicioResult> obtenerPorAsignacionYRol(
            UUID solicitudId,
            UUID asignacionServicioId,
            RolConfirmante rolCalificador
    );
}
