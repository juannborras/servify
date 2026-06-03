package com.servify.solicitudes.application.service;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.ActualizarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.DisponibilidadHorariaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.UbicacionSolicitudResult;
import com.servify.solicitudes.application.port.in.ActualizarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.UUID;

public class ActualizarSolicitudServicioService implements ActualizarSolicitudServicioUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribuidorSolicitudService distribuidorSolicitudService;

    public ActualizarSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this(solicitudServicioRepositoryPort, null);
    }

    public ActualizarSolicitudServicioService(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribuidorSolicitudService distribuidorSolicitudService
    ) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribuidorSolicitudService = distribuidorSolicitudService;
    }

    @Override
    public SolicitudServicioResult actualizar(ActualizarSolicitudServicioCommand command) {
        validarCommand(command);
        SolicitudServicio solicitud = obtenerSolicitudExistente(command.getSolicitudId());
        validarPertenenciaSolicitante(solicitud, command.getSolicitanteId());
        validarEdicionPermitida(solicitud);

        aplicarCambios(solicitud, command);

        SolicitudServicio guardada = solicitudServicioRepositoryPort.guardar(solicitud);
        redistribuirSiCorresponde(guardada);
        return construirResultado(guardada);
    }

    protected void redistribuirSiCorresponde(SolicitudServicio solicitud) {
        if (distribuidorSolicitudService != null) {
            distribuidorSolicitudService.distribuir(solicitud);
        }
    }

    protected void validarCommand(ActualizarSolicitudServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected void validarPertenenciaSolicitante(SolicitudServicio solicitud, UUID solicitanteId) {
        if (!solicitud.getSolicitanteId().equals(solicitanteId)) {
            throw new IllegalArgumentException("El solicitante no es propietario de la solicitud");
        }
    }

    protected void validarEdicionPermitida(SolicitudServicio solicitud) {
        if (!solicitud.estaBuscandoPrestador()) {
            throw new IllegalStateException("Solo se pueden editar solicitudes activas sin asignacion confirmada");
        }
    }

    protected void aplicarCambios(SolicitudServicio solicitud, ActualizarSolicitudServicioCommand command) {
        if (command.getModalidadServicio() != null) {
            solicitud.actualizarModalidad(command.getModalidadServicio());
        }
        if (command.getUbicacion() != null) {
            solicitud.actualizarUbicacion(command.getUbicacion());
        }
        if (command.getDisponibilidadRequerida() != null) {
            solicitud.actualizarDisponibilidadRequerida(command.getDisponibilidadRequerida());
        }
        if (command.getDescripcionNecesidad() != null && !command.getDescripcionNecesidad().isBlank()) {
            solicitud.actualizarDescripcionNecesidad(command.getDescripcionNecesidad());
        }
        if (command.getPrecioReferencia() != null) {
            solicitud.actualizarPrecioReferencia(command.getPrecioReferencia());
        }
    }

    protected SolicitudServicioResult construirResultado(SolicitudServicio solicitudServicio) {
        return new SolicitudServicioResult(
                solicitudServicio.getId(),
                solicitudServicio.getSolicitanteId(),
                solicitudServicio.getCategoriaServicioId(),
                solicitudServicio.getModalidadServicio(),
                construirUbicacionResult(solicitudServicio.getUbicacion()),
                construirDisponibilidadResult(solicitudServicio.getDisponibilidadRequerida()),
                solicitudServicio.getDescripcionNecesidad(),
                solicitudServicio.getPrecioReferencia(),
                solicitudServicio.getEstado(),
                solicitudServicio.getFechaSolicitud()
        );
    }

    protected UbicacionSolicitudResult construirUbicacionResult(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }
        return new UbicacionSolicitudResult(
                ubicacion.getPais(),
                ubicacion.getProvincia(),
                ubicacion.getCiudad(),
                ubicacion.getLocalidad(),
                ubicacion.getCalle(),
                ubicacion.getAltura(),
                ubicacion.getReferencia(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud()
        );
    }

    protected DisponibilidadHorariaResult construirDisponibilidadResult(DisponibilidadHoraria disponibilidadHoraria) {
        if (disponibilidadHoraria == null) {
            return null;
        }
        return new DisponibilidadHorariaResult(
                disponibilidadHoraria.getDiaSemana(),
                disponibilidadHoraria.getHoraDesde(),
                disponibilidadHoraria.getHoraHasta()
        );
    }
}
