package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.AcordarPrecioAsignacionCommand;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.port.in.AcordarPrecioAsignacionUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public class AcordarPrecioAsignacionService implements AcordarPrecioAsignacionUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;

    public AcordarPrecioAsignacionService(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort
    ) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
    }

    @Override
    @Transactional
    public AsignacionServicioResult acordar(AcordarPrecioAsignacionCommand command) {
        validarCommand(command);
        SolicitudServicio solicitud = solicitudServicioRepositoryPort.buscarPorId(command.getSolicitudId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + command.getSolicitudId()));
        if (!solicitud.getSolicitanteId().equals(command.getSolicitanteId())) {
            throw new IllegalArgumentException("Solo el solicitante puede registrar el precio acordado");
        }

        AsignacionServicio asignacion = asignacionServicioRepositoryPort
                .buscarPorIdParaActualizar(command.getAsignacionServicioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Asignacion no encontrada: " + command.getAsignacionServicioId()));
        if (!asignacion.correspondeASolicitud(command.getSolicitudId())) {
            throw new IllegalArgumentException("La asignacion no corresponde a la solicitud indicada");
        }
        if (asignacion.estaCancelada() || asignacion.estaFinalizada()) {
            throw new IllegalStateException("No se puede acordar el precio de una asignacion cerrada");
        }

        BigDecimal precioActual = asignacion.getPrecioAcordado();
        if (precioActual != null && precioActual.signum() > 0) {
            if (precioActual.compareTo(command.getPrecioAcordado()) == 0) {
                return construirResultado(asignacion);
            }
            throw new IllegalStateException("El precio ya fue acordado para esta asignacion");
        }

        asignacion.actualizarPrecioAcordado(command.getPrecioAcordado());
        return construirResultado(asignacionServicioRepositoryPort.guardar(asignacion));
    }

    private void validarCommand(AcordarPrecioAsignacionCommand command) {
        if (command == null) throw new IllegalArgumentException("El comando no puede ser nulo");
        if (command.getSolicitudId() == null) throw new IllegalArgumentException("solicitudId no puede ser nulo");
        if (command.getAsignacionServicioId() == null) {
            throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        if (command.getPrecioAcordado() == null || command.getPrecioAcordado().signum() <= 0) {
            throw new IllegalArgumentException("El precio acordado debe ser mayor a cero");
        }
    }

    private AsignacionServicioResult construirResultado(AsignacionServicio asignacion) {
        return AsignacionServicioResult.builder()
                .id(asignacion.getId())
                .solicitudId(asignacion.getSolicitudId())
                .distribucionSolicitudId(asignacion.getDistribucionSolicitudId())
                .prestadorId(asignacion.getPrestadorId())
                .publicacionServicioId(asignacion.getPublicacionServicioId())
                .precioAcordado(asignacion.getPrecioAcordado())
                .estado(asignacion.getEstado())
                .fechaAsignacion(asignacion.getFechaAsignacion())
                .fechaFinalizacion(asignacion.getFechaFinalizacion())
                .build();
    }
}
