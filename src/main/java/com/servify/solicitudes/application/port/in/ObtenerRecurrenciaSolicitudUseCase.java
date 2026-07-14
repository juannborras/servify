package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;

import java.util.Optional;
import java.util.UUID;

public interface ObtenerRecurrenciaSolicitudUseCase {

    Optional<ServicioRecurrenciaResult> obtenerPorSolicitudId(UUID solicitudId);
}
