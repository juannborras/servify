package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConfirmarFinalizacionServicioService implements ConfirmarFinalizacionServicioUseCase {

    private final ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final PoliticaFinalizacionMutua politicaFinalizacionMutua;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua
    ) {
        this(
                confirmacionFinalizacionRepositoryPort,
                asignacionServicioRepositoryPort,
                solicitudServicioRepositoryPort,
                politicaFinalizacionMutua,
                null
        );
    }

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        this.confirmacionFinalizacionRepositoryPort = confirmacionFinalizacionRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.politicaFinalizacionMutua = politicaFinalizacionMutua;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public void confirmar(ConfirmarFinalizacionServicioCommand command) {
        // Confirma la finalización de un servicio por una de las partes.
        // - valida que la asignación y la solicitud correspondan
        // - registra la confirmación y cierra la asignación si la política lo permite
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getAsignacionServicioId() == null) {
            throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        }
        if (command.getConfirmanteId() == null) {
            throw new IllegalArgumentException("confirmanteId no puede ser nulo");
        }
        if (command.getRolConfirmante() == null) {
            throw new IllegalArgumentException("rolConfirmante no puede ser nulo");
        }

        SolicitudServicio solicitud = obtenerSolicitudExistente(command.getSolicitudId());
        AsignacionServicio asignacion = obtenerAsignacionExistente(command.getAsignacionServicioId());

        validarCorrespondenciaSolicitudAsignacion(solicitud, asignacion);
        validarAsignacionFinalizable(asignacion);
        validarConfirmante(command, asignacion, solicitud);
        validarAusenciaDeConfirmacionPrevia(command);

        LocalDateTime fecha = obtenerFechaActual();
        ConfirmacionFinalizacion confirmacion = construirConfirmacion(command, fecha);
        persistirConfirmacion(confirmacion);

        boolean yaFinalizada = asignacion.estaFinalizada();
        List<ConfirmacionFinalizacion> confirmaciones = obtenerConfirmacionesDeAsignacion(asignacion.getId());
        evaluarYCerrarSiCorresponde(solicitud, asignacion, confirmaciones, fecha);

        persistirAsignacion(asignacion);
        persistirSolicitud(solicitud);
        notificarFinalizacion(solicitud, asignacion, command.getRolConfirmante(), yaFinalizada);
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected AsignacionServicio obtenerAsignacionExistente(UUID asignacionServicioId) {
        if (asignacionServicioId == null) {
            throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        }
        return this.asignacionServicioRepositoryPort.buscarPorId(asignacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada: " + asignacionServicioId));
    }

    protected void validarCorrespondenciaSolicitudAsignacion(SolicitudServicio solicitudServicio,
                                                             AsignacionServicio asignacionServicio) {
        if (solicitudServicio == null || asignacionServicio == null) {
            throw new IllegalArgumentException("Solicitud y asignación no pueden ser nulas");
        }
        if (!asignacionServicio.correspondeASolicitud(solicitudServicio.getId())) {
            throw new IllegalArgumentException("La asignación no corresponde a la solicitud indicada");
        }
    }

    protected void validarAsignacionFinalizable(AsignacionServicio asignacionServicio) {
        if (asignacionServicio == null) {
            throw new IllegalArgumentException("Asignación no puede ser nula");
        }
        if (!asignacionServicio.puedeFinalizarse()) {
            throw new IllegalStateException("La asignación no está en estado apto para finalizarse");
        }
    }

    protected void validarConfirmante(ConfirmarFinalizacionServicioCommand command,
                                      AsignacionServicio asignacionServicio,
                                      SolicitudServicio solicitudServicio) {
        if (command == null || asignacionServicio == null || solicitudServicio == null) {
            throw new IllegalArgumentException("Argumentos inválidos para validar confirmante");
        }
        switch (command.getRolConfirmante()) {
            case SOLICITANTE:
                if (!solicitudServicio.getSolicitanteId().equals(command.getConfirmanteId())) {
                    throw new IllegalArgumentException("El confirmante no es el solicitante de la solicitud");
                }
                break;
            case PRESTADOR:
                if (!asignacionServicio.getPrestadorId().equals(command.getConfirmanteId())) {
                    throw new IllegalArgumentException("El confirmante no es el prestador de la asignación");
                }
                break;
            default:
                throw new IllegalArgumentException("Rol de confirmante no reconocido: " + command.getRolConfirmante());
        }
    }

    protected void validarAusenciaDeConfirmacionPrevia(ConfirmarFinalizacionServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        java.util.Optional<ConfirmacionFinalizacion> previa = this.confirmacionFinalizacionRepositoryPort
                .buscarPorAsignacionServicioIdYRolConfirmante(command.getAsignacionServicioId(), command.getRolConfirmante());
        if (previa.isPresent() && previa.get().estaConfirmada()) {
            throw new IllegalStateException("Ya existe una confirmación previa válida para este rol y asignación");
        }
    }

    protected ConfirmacionFinalizacion construirConfirmacion(ConfirmarFinalizacionServicioCommand command,
                                                             LocalDateTime fechaConfirmacion) {
        if (command == null || fechaConfirmacion == null) {
            throw new IllegalArgumentException("Argumentos inválidos para construir confirmación");
        }
        ConfirmacionFinalizacion c = new ConfirmacionFinalizacion(
                generarIdConfirmacion(),
                command.getSolicitudId(),
                command.getAsignacionServicioId(),
                command.getConfirmanteId(),
                command.getRolConfirmante(),
                true,
                fechaConfirmacion,
                command.getObservacion()
        );
        return c;
    }

    protected List<ConfirmacionFinalizacion> obtenerConfirmacionesDeAsignacion(UUID asignacionServicioId) {
        if (asignacionServicioId == null) {
            throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        }
        return this.confirmacionFinalizacionRepositoryPort.buscarPorAsignacionServicioId(asignacionServicioId);
    }

    protected void evaluarYCerrarSiCorresponde(SolicitudServicio solicitudServicio,
                                               AsignacionServicio asignacionServicio,
                                               List<ConfirmacionFinalizacion> confirmaciones,
                                               LocalDateTime fechaFinalizacion) {
        if (solicitudServicio == null || asignacionServicio == null || confirmaciones == null) {
            throw new IllegalArgumentException("Argumentos inválidos para evaluación de cierre");
        }
        if (this.politicaFinalizacionMutua.puedeFinalizarse(solicitudServicio, confirmaciones)) {
            asignacionServicio.finalizar(fechaFinalizacion);
            solicitudServicio.marcarComoFinalizada();
        }
    }

    protected void persistirConfirmacion(ConfirmacionFinalizacion confirmacionFinalizacion) {
        if (confirmacionFinalizacion == null) {
            throw new IllegalArgumentException("confirmacionFinalizacion no puede ser nula");
        }
        this.confirmacionFinalizacionRepositoryPort.guardar(confirmacionFinalizacion);
    }

    protected void persistirAsignacion(AsignacionServicio asignacionServicio) {
        if (asignacionServicio == null) {
            throw new IllegalArgumentException("asignacionServicio no puede ser nula");
        }
        this.asignacionServicioRepositoryPort.guardar(asignacionServicio);
    }

    protected void persistirSolicitud(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("solicitudServicio no puede ser nula");
        }
        this.solicitudServicioRepositoryPort.guardar(solicitudServicio);
    }

    protected UUID generarIdConfirmacion() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    protected void notificarFinalizacion(
            SolicitudServicio solicitud,
            AsignacionServicio asignacion,
            com.servify.solicitudes.domain.enumtype.RolConfirmante rolConfirmante,
            boolean yaFinalizada
    ) {
        if (notificadorEventosSolicitudService == null) {
            return;
        }
        if (!yaFinalizada && asignacion.estaFinalizada()) {
            notificadorEventosSolicitudService.servicioFinalizado(solicitud, asignacion);
            return;
        }
        notificadorEventosSolicitudService.confirmacionFinalizacion(solicitud, asignacion, rolConfirmante);
    }
}
