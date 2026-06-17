package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.CancelarSolicitudServicioCommand;
import com.servify.solicitudes.application.port.in.CancelarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.List;
import java.util.UUID;

public class CancelarSolicitudServicioService implements CancelarSolicitudServicioUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public CancelarSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this(solicitudServicioRepositoryPort, null, null);
    }

    public CancelarSolicitudServicioService(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public void cancelar(CancelarSolicitudServicioCommand command) {
        // Cancela una solicitud por su solicitante si el estado lo permite.
        // - valida pertenencia y reglas de cancelación
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }

        SolicitudServicio solicitud = obtenerSolicitudExistente(command.getSolicitudId());
        validarPertenenciaSolicitante(solicitud, command.getSolicitanteId());
        validarCancelacionPermitida(solicitud);

        solicitud.cancelar();
        persistirSolicitud(solicitud);
        notificarCancelacion(solicitud);
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected void validarPertenenciaSolicitante(SolicitudServicio solicitudServicio, UUID solicitanteId) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("Solicitud no puede ser nula");
        }
        if (solicitanteId == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        if (!solicitudServicio.getSolicitanteId().equals(solicitanteId)) {
            throw new IllegalArgumentException("El solicitante no es propietario de la solicitud");
        }
    }

    protected void validarCancelacionPermitida(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("Solicitud no puede ser nula");
        }
        if (!solicitudServicio.puedeSerCancelada()) {
            throw new IllegalStateException("La solicitud no puede cancelarse en su estado actual");
        }
    }

    protected void persistirSolicitud(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("Solicitud no puede ser nula");
        }
        this.solicitudServicioRepositoryPort.guardar(solicitudServicio);
    }

    protected void notificarCancelacion(SolicitudServicio solicitud) {
        if (notificadorEventosSolicitudService == null || distribucionSolicitudRepositoryPort == null) {
            return;
        }
        List<DistribucionSolicitud> distribuciones = distribucionSolicitudRepositoryPort.buscarPorSolicitudId(solicitud.getId());
        notificadorEventosSolicitudService.solicitudCancelada(solicitud, distribuciones);
    }
}
