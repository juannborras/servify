package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.ProponerEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;

public interface ProponerEncuentroServicioUseCase {

    ServicioEncuentroResult proponer(ProponerEncuentroServicioCommand command);
}
