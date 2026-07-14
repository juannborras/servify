package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.CancelarEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;

public interface CancelarEncuentroServicioUseCase {

    ServicioEncuentroResult cancelar(CancelarEncuentroServicioCommand command);
}
