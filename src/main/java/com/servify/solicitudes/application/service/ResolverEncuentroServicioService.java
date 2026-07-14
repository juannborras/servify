package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ResolverEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.port.in.ResolverEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ResolverEncuentroServicioService implements ResolverEncuentroServicioUseCase {

    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public ResolverEncuentroServicioService(ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                            NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    @Transactional
    public ServicioEncuentroResult resolver(ResolverEncuentroServicioCommand command) {
        validarCommand(command);
        ServicioEncuentro encuentro = obtenerEncuentro(command.getEncuentroId());
        SolicitudServicio solicitud = obtenerSolicitud(encuentro.getSolicitudId());
        AsignacionServicio asignacion = obtenerAsignacion(encuentro);
        validarParticipante(solicitud, asignacion, command.getUsuarioId());
        if (encuentro.fuePropuestoPor(command.getUsuarioId())) {
            throw new IllegalStateException("Quien propuso el encuentro debe esperar respuesta de la otra parte");
        }

        if (command.getDecision() == TipoDecisionSolicitud.ACEPTAR) {
            encuentro.confirmar(LocalDateTime.now());
        } else {
            encuentro.rechazar(LocalDateTime.now());
        }

        ServicioEncuentro guardado = servicioEncuentroRepositoryPort.guardar(encuentro);
        notificarResolucion(solicitud, asignacion, guardado, command.getUsuarioId());
        return ServicioAgendaMapper.toEncuentroResult(guardado);
    }

    private void validarCommand(ResolverEncuentroServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getEncuentroId() == null) {
            throw new IllegalArgumentException("encuentroId no puede ser nulo");
        }
        if (command.getUsuarioId() == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        if (command.getDecision() == null) {
            throw new IllegalArgumentException("decision no puede ser nula");
        }
    }

    private ServicioEncuentro obtenerEncuentro(UUID encuentroId) {
        return servicioEncuentroRepositoryPort.buscarPorIdParaActualizar(encuentroId)
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado: " + encuentroId));
    }

    private SolicitudServicio obtenerSolicitud(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    private AsignacionServicio obtenerAsignacion(ServicioEncuentro encuentro) {
        return asignacionServicioRepositoryPort.buscarPorId(encuentro.getAsignacionServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada: " + encuentro.getAsignacionServicioId()));
    }

    private void validarParticipante(SolicitudServicio solicitud, AsignacionServicio asignacion, UUID usuarioId) {
        boolean esSolicitante = Objects.equals(solicitud.getSolicitanteId(), usuarioId);
        boolean esPrestador = Objects.equals(asignacion.getPrestadorId(), usuarioId);
        if (!esSolicitante && !esPrestador) {
            throw new IllegalArgumentException("El usuario no participa de esta solicitud");
        }
    }

    private void notificarResolucion(SolicitudServicio solicitud,
                                     AsignacionServicio asignacion,
                                     ServicioEncuentro encuentro,
                                     UUID resueltoPorId) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.encuentroResuelto(solicitud, asignacion, encuentro, resueltoPorId);
        }
    }
}
