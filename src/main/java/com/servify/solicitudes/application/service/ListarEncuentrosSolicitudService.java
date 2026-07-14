package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.port.in.ListarEncuentrosSolicitudUseCase;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ListarEncuentrosSolicitudService implements ListarEncuentrosSolicitudUseCase {

    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;

    public ListarEncuentrosSolicitudService(ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort) {
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
    }

    @Override
    public List<ServicioEncuentroResult> listarPorSolicitudId(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return servicioEncuentroRepositoryPort.buscarPorSolicitudId(solicitudId).stream()
                .sorted(Comparator.comparing(
                        encuentro -> encuentro.getFechaInicio() == null ? java.time.LocalDateTime.MIN : encuentro.getFechaInicio()
                ))
                .map(ServicioAgendaMapper::toEncuentroResult)
                .toList();
    }
}
