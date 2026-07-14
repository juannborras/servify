package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.CancelarRecurrenciaServicioCommand;
import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;
import com.servify.solicitudes.application.port.in.CancelarRecurrenciaServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CancelarRecurrenciaServicioService implements CancelarRecurrenciaServicioUseCase {

    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public CancelarRecurrenciaServicioService(ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort,
                                              SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                              AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                              NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this(
                servicioRecurrenciaRepositoryPort,
                solicitudServicioRepositoryPort,
                asignacionServicioRepositoryPort,
                null,
                notificadorEventosSolicitudService
        );
    }

    public CancelarRecurrenciaServicioService(ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort,
                                              SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                              AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                              ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                              NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    @Transactional
    public ServicioRecurrenciaResult cancelar(CancelarRecurrenciaServicioCommand command) {
        validarCommand(command);
        ServicioRecurrencia referencia = servicioRecurrenciaRepositoryPort.buscarPorSolicitudId(command.getSolicitudId())
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no tiene recurrencia"));
        ServicioRecurrencia recurrencia = servicioRecurrenciaRepositoryPort
                .buscarPorIdParaActualizar(referencia.getId())
                .orElseThrow(() -> new IllegalArgumentException("La recurrencia ya no existe"));
        SolicitudServicio solicitud = obtenerSolicitud(command.getSolicitudId());
        Optional<AsignacionServicio> asignacion = obtenerAsignacion(recurrencia);
        validarParticipante(solicitud, asignacion, command.getUsuarioId());

        LocalDateTime fechaCancelacion = LocalDateTime.now();
        boolean yaEstabaCancelada = recurrencia.estaCancelada();
        ServicioRecurrencia guardada = recurrencia;
        if (!yaEstabaCancelada) {
            validarSolicitudCancelable(solicitud);
            recurrencia.cancelar(command.getUsuarioId(), command.getMotivo(), fechaCancelacion);
            guardada = servicioRecurrenciaRepositoryPort.guardar(recurrencia);
        }

        cancelarEncuentrosAbiertos(solicitud.getId(), fechaCancelacion);
        cancelarSolicitudYAsignacion(solicitud, asignacion);
        if (!yaEstabaCancelada) {
            notificarCancelacion(solicitud, asignacion.orElse(null), guardada, command.getUsuarioId());
        }
        return ServicioAgendaMapper.toRecurrenciaResult(guardada);
    }

    private void cancelarSolicitudYAsignacion(SolicitudServicio solicitud,
                                               Optional<AsignacionServicio> asignacion) {
        if (!solicitud.estaCancelada() && !solicitud.estaFinalizada()) {
            solicitud.cancelar();
            solicitudServicioRepositoryPort.guardar(solicitud);
        }
        asignacion.filter(actual -> !actual.estaCancelada() && !actual.estaFinalizada())
                .ifPresent(actual -> {
                    actual.cancelar();
                    asignacionServicioRepositoryPort.guardar(actual);
                });
    }

    private void cancelarEncuentrosAbiertos(UUID solicitudId, LocalDateTime fechaCancelacion) {
        if (servicioEncuentroRepositoryPort == null) {
            return;
        }
        servicioEncuentroRepositoryPort.buscarPorSolicitudId(solicitudId).stream()
                .filter(Objects::nonNull)
                .map(ServicioEncuentro::getId)
                .sorted()
                .map(servicioEncuentroRepositoryPort::buscarPorIdParaActualizar)
                .flatMap(Optional::stream)
                .filter(encuentro -> !encuentro.estaCerrado())
                .forEach(encuentro -> cancelarEncuentro(encuentro, fechaCancelacion));
    }

    private void cancelarEncuentro(ServicioEncuentro encuentro, LocalDateTime fechaCancelacion) {
        encuentro.cancelar(fechaCancelacion);
        servicioEncuentroRepositoryPort.guardar(encuentro);
    }

    private void validarSolicitudCancelable(SolicitudServicio solicitud) {
        if (!solicitud.puedeSerCancelada()) {
            throw new IllegalStateException("La solicitud no puede cancelarse en su estado actual");
        }
    }

    private void validarCommand(CancelarRecurrenciaServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getUsuarioId() == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
    }

    private SolicitudServicio obtenerSolicitud(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    private Optional<AsignacionServicio> obtenerAsignacion(ServicioRecurrencia recurrencia) {
        if (recurrencia.getAsignacionServicioId() != null) {
            return asignacionServicioRepositoryPort.buscarPorId(recurrencia.getAsignacionServicioId());
        }
        return asignacionServicioRepositoryPort.buscarPorSolicitudId(recurrencia.getSolicitudId());
    }

    private void validarParticipante(SolicitudServicio solicitud, Optional<AsignacionServicio> asignacion, UUID usuarioId) {
        if (Objects.equals(solicitud.getSolicitanteId(), usuarioId)) {
            return;
        }
        if (asignacion.isPresent() && Objects.equals(asignacion.get().getPrestadorId(), usuarioId)) {
            return;
        }
        throw new IllegalArgumentException("El usuario no participa de esta recurrencia");
    }

    private void notificarCancelacion(SolicitudServicio solicitud,
                                      AsignacionServicio asignacion,
                                      ServicioRecurrencia recurrencia,
                                      UUID canceladaPorId) {
        if (notificadorEventosSolicitudService != null && asignacion != null) {
            notificadorEventosSolicitudService.recurrenciaCancelada(solicitud, asignacion, recurrencia, canceladaPorId);
        }
    }
}
