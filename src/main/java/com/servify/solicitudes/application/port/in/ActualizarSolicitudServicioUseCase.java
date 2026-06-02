package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.ActualizarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;

public interface ActualizarSolicitudServicioUseCase {

    SolicitudServicioResult actualizar(ActualizarSolicitudServicioCommand command);
}
