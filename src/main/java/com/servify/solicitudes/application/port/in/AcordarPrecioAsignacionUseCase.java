package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.AcordarPrecioAsignacionCommand;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;

public interface AcordarPrecioAsignacionUseCase {

    AsignacionServicioResult acordar(AcordarPrecioAsignacionCommand command);
}
