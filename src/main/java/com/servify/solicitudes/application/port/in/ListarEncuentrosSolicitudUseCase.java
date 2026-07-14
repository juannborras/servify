package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.ServicioEncuentroResult;

import java.util.List;
import java.util.UUID;

public interface ListarEncuentrosSolicitudUseCase {

    List<ServicioEncuentroResult> listarPorSolicitudId(UUID solicitudId);
}
