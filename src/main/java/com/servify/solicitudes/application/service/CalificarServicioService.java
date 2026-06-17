package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.CalificarServicioCommand;
import com.servify.solicitudes.application.port.in.CalificarServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.CalificacionRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.Calificacion;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.PoliticaCalificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CalificarServicioService implements CalificarServicioUseCase {

    private final CalificacionRepositoryPort calificacionRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final PoliticaCalificacion politicaCalificacion;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public CalificarServicioService(CalificacionRepositoryPort calificacionRepositoryPort,
                                    SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                    AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                    PoliticaCalificacion politicaCalificacion) {
        this(
                calificacionRepositoryPort,
                solicitudServicioRepositoryPort,
                asignacionServicioRepositoryPort,
                politicaCalificacion,
                null
        );
    }

    public CalificarServicioService(CalificacionRepositoryPort calificacionRepositoryPort,
                                    SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                    AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                    PoliticaCalificacion politicaCalificacion,
                                    NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.calificacionRepositoryPort = calificacionRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.politicaCalificacion = politicaCalificacion;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public void calificar(CalificarServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando de calificacion no puede ser nulo");
        }
        if (command.getSolicitudId() == null || command.getAsignacionServicioId() == null ||
                command.getSolicitanteId() == null || command.getPrestadorId() == null ||
                command.getCalificadorIdOrDefault() == null || command.getRolCalificadorOrDefault() == null ||
                command.getPuntaje() == null) {
            throw new IllegalArgumentException("Todos los campos del comando de calificacion son obligatorios");
        }

        SolicitudServicio solicitudServicio = obtenerSolicitudExistente(command.getSolicitudId());
        AsignacionServicio asignacionServicio = obtenerAsignacionExistente(command.getAsignacionServicioId());

        validarCorrespondenciaSolicitudAsignacion(solicitudServicio, asignacionServicio);
        validarParticipantesAsignacion(solicitudServicio, asignacionServicio, command);
        validarCalificador(
                solicitudServicio,
                asignacionServicio,
                command.getCalificadorIdOrDefault(),
                command.getRolCalificadorOrDefault()
        );
        validarCalificacionPermitida(
                solicitudServicio,
                asignacionServicio,
                command.getRolCalificadorOrDefault(),
                command.getPuntaje()
        );

        Calificacion calificacion = construirCalificacion(
                command,
                solicitudServicio,
                asignacionServicio,
                obtenerFechaActual()
        );
        persistirCalificacion(calificacion);
        notificarCalificacion(calificacion, solicitudServicio);
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("El ID de la solicitud no puede ser nulo");
        }

        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud con ID " + solicitudId + " no existe"));
    }

    protected AsignacionServicio obtenerAsignacionExistente(UUID asignacionServicioId) {
        if (asignacionServicioId == null) {
            throw new IllegalArgumentException("El ID de la asignacion no puede ser nulo");
        }

        return asignacionServicioRepositoryPort.buscarPorId(asignacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La asignacion con ID " + asignacionServicioId + " no existe"));
    }

    protected void validarCorrespondenciaSolicitudAsignacion(SolicitudServicio solicitudServicio,
                                                             AsignacionServicio asignacionServicio) {
        if (!asignacionServicio.getSolicitudId().equals(solicitudServicio.getId())) {
            throw new IllegalArgumentException("La asignacion no corresponde a la solicitud indicada");
        }
    }

    protected void validarParticipantesAsignacion(SolicitudServicio solicitudServicio,
                                                  AsignacionServicio asignacionServicio,
                                                  CalificarServicioCommand command) {
        if (!solicitudServicio.getSolicitanteId().equals(command.getSolicitanteId())) {
            throw new IllegalArgumentException("El solicitante indicado no corresponde a la solicitud");
        }
        if (!asignacionServicio.getPrestadorId().equals(command.getPrestadorId())) {
            throw new IllegalArgumentException("El prestador indicado no corresponde al asignado");
        }
    }

    protected void validarCalificador(SolicitudServicio solicitudServicio,
                                      AsignacionServicio asignacionServicio,
                                      UUID calificadorId,
                                      RolConfirmante rolCalificador) {
        if (rolCalificador == RolConfirmante.SOLICITANTE
                && !solicitudServicio.getSolicitanteId().equals(calificadorId)) {
            throw new IllegalArgumentException("Solo el solicitante puede emitir esta calificacion");
        }
        if (rolCalificador == RolConfirmante.PRESTADOR
                && !asignacionServicio.getPrestadorId().equals(calificadorId)) {
            throw new IllegalArgumentException("Solo el prestador asignado puede emitir esta calificacion");
        }
    }

    protected void validarCalificacionPermitida(SolicitudServicio solicitudServicio,
                                                AsignacionServicio asignacionServicio,
                                                RolConfirmante rolCalificador,
                                                Integer puntaje) {
        List<Calificacion> calificacionesExistentes = calificacionRepositoryPort
                .buscarPorAsignacionServicioIdYRolCalificador(asignacionServicio.getId(), rolCalificador)
                .map(List::of)
                .orElseGet(List::of);

        if (!politicaCalificacion.puedeCalificarse(
                solicitudServicio,
                calificacionesExistentes,
                asignacionServicio.getPrestadorId()
        )) {
            throw new IllegalArgumentException(
                    "No se puede calificar este servicio. Verifique que la solicitud este finalizada " +
                            "y que no exista una calificacion previa para este rol");
        }

        if (!politicaCalificacion.puntajePermitido(puntaje)) {
            throw new IllegalArgumentException("El puntaje debe ser un valor entre 1 y 5");
        }
    }

    protected Calificacion construirCalificacion(CalificarServicioCommand command,
                                                 SolicitudServicio solicitudServicio,
                                                 AsignacionServicio asignacionServicio,
                                                 LocalDateTime fechaCalificacion) {
        UUID calificacionId = generarIdCalificacion();
        RolConfirmante rol = command.getRolCalificadorOrDefault();
        UUID calificadorId = command.getCalificadorIdOrDefault();
        UUID calificadoId = rol == RolConfirmante.PRESTADOR
                ? solicitudServicio.getSolicitanteId()
                : asignacionServicio.getPrestadorId();

        return new Calificacion(
                calificacionId,
                command.getSolicitudId(),
                command.getAsignacionServicioId(),
                command.getSolicitanteId(),
                command.getPrestadorId(),
                calificadorId,
                calificadoId,
                rol,
                command.getPuntaje(),
                command.getComentario(),
                fechaCalificacion
        );
    }

    protected void persistirCalificacion(Calificacion calificacion) {
        calificacionRepositoryPort.guardar(calificacion);
    }

    protected UUID generarIdCalificacion() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    protected void notificarCalificacion(Calificacion calificacion, SolicitudServicio solicitudServicio) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.calificacionRecibida(
                    calificacion.getCalificadoId(),
                    solicitudServicio,
                    calificacion.getAsignacionServicioId()
            );
        }
    }
}
