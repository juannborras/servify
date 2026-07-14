package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.ResolverEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;

public interface ResolverEncuentroServicioUseCase {

    ServicioEncuentroResult resolver(ResolverEncuentroServicioCommand command);
}
