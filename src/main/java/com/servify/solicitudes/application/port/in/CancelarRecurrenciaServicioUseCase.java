package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.CancelarRecurrenciaServicioCommand;
import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;

public interface CancelarRecurrenciaServicioUseCase {

    ServicioRecurrenciaResult cancelar(CancelarRecurrenciaServicioCommand command);
}
