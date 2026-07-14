package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ProponerEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.port.in.ProponerEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ProponerEncuentroServicioService implements ProponerEncuentroServicioUseCase {

    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public ProponerEncuentroServicioService(ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                            NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public ServicioEncuentroResult proponer(ProponerEncuentroServicioCommand command) {
        validarCommand(command);
        SolicitudServicio solicitud = obtenerSolicitud(command.getSolicitudId());
        AsignacionServicio asignacion = obtenerAsignacion(solicitud.getId(), command.getAsignacionServicioId());
        validarEstadosOperativos(solicitud, asignacion);
        validarParticipante(solicitud, asignacion, command.getPropuestoPorId());

        ServicioEncuentro encuentro = new ServicioEncuentro(
                UUID.randomUUID(),
                solicitud.getId(),
                asignacion.getId(),
                command.getPropuestoPorId(),
                command.getFechaInicio(),
                command.getFechaFin(),
                EstadoEncuentroServicio.PROPUESTO,
                command.getMensaje(),
                null
        );

        ServicioEncuentro guardado = servicioEncuentroRepositoryPort.guardar(encuentro);
        notificarPropuesta(solicitud, asignacion, guardado);
        return ServicioAgendaMapper.toEncuentroResult(guardado);
    }

    private void validarCommand(ProponerEncuentroServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getPropuestoPorId() == null) {
            throw new IllegalArgumentException("propuestoPorId no puede ser nulo");
        }
        if (command.getFechaInicio() == null || command.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas del encuentro son obligatorias");
        }
        if (!command.getFechaInicio().isBefore(command.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        if (command.getFechaInicio().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("No se puede proponer un encuentro en el pasado");
        }
    }

    private SolicitudServicio obtenerSolicitud(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    private AsignacionServicio obtenerAsignacion(UUID solicitudId, UUID asignacionServicioId) {
        AsignacionServicio asignacion = asignacionServicioId != null
                ? asignacionServicioRepositoryPort.buscarPorId(asignacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada: " + asignacionServicioId))
                : asignacionServicioRepositoryPort.buscarPorSolicitudId(solicitudId)
                .orElseThrow(() -> new IllegalStateException("La solicitud no tiene prestador asignado"));
        if (!Objects.equals(asignacion.getSolicitudId(), solicitudId)) {
            throw new IllegalStateException("La asignacion no corresponde a la solicitud indicada");
        }
        return asignacion;
    }

    private void validarParticipante(SolicitudServicio solicitud, AsignacionServicio asignacion, UUID usuarioId) {
        boolean esSolicitante = Objects.equals(solicitud.getSolicitanteId(), usuarioId);
        boolean esPrestador = Objects.equals(asignacion.getPrestadorId(), usuarioId);
        if (!esSolicitante && !esPrestador) {
            throw new IllegalArgumentException("El usuario no participa de esta solicitud");
        }
    }

    private void validarEstadosOperativos(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        if (!solicitud.estaAsignada()) {
            throw new IllegalStateException("Solo se pueden proponer encuentros para una solicitud asignada");
        }
        if (!asignacion.estaActiva()) {
            throw new IllegalStateException("Solo se pueden proponer encuentros para una asignacion activa");
        }
    }

    private void notificarPropuesta(SolicitudServicio solicitud, AsignacionServicio asignacion, ServicioEncuentro encuentro) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.encuentroPropuesto(solicitud, asignacion, encuentro);
        }
    }
}
