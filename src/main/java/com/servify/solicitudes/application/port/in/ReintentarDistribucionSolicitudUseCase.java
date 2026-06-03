package com.servify.solicitudes.application.port.in;

import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;

import java.util.List;
import java.util.UUID;

public interface ReintentarDistribucionSolicitudUseCase {

    List<DistribucionSolicitudResult> reintentar(UUID solicitudId);
}
