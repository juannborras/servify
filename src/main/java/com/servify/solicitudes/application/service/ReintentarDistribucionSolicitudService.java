package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;
import com.servify.solicitudes.application.port.in.ReintentarDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.List;
import java.util.UUID;

public class ReintentarDistribucionSolicitudService implements ReintentarDistribucionSolicitudUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribuidorSolicitudService distribuidorSolicitudService;

    public ReintentarDistribucionSolicitudService(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribuidorSolicitudService distribuidorSolicitudService
    ) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribuidorSolicitudService = distribuidorSolicitudService;
    }

    @Override
    public List<DistribucionSolicitudResult> reintentar(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        SolicitudServicio solicitud = solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
        if (!solicitud.estaBuscandoPrestador()) {
            throw new IllegalStateException("Solo se pueden reintentar solicitudes activas sin asignacion confirmada");
        }

        return distribuidorSolicitudService.distribuir(solicitud).stream()
                .map(distribuidorSolicitudService::construirResultado)
                .toList();
    }
}
