package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;
import com.servify.solicitudes.application.port.in.ObtenerRecurrenciaSolicitudUseCase;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;

import java.util.Optional;
import java.util.UUID;

public class ObtenerRecurrenciaSolicitudService implements ObtenerRecurrenciaSolicitudUseCase {

    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;

    public ObtenerRecurrenciaSolicitudService(ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort) {
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
    }

    @Override
    public Optional<ServicioRecurrenciaResult> obtenerPorSolicitudId(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return servicioRecurrenciaRepositoryPort.buscarPorSolicitudId(solicitudId)
                .map(ServicioAgendaMapper::toRecurrenciaResult);
    }
}
