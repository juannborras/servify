package com.servify.pagos.application.service;

import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.port.in.ConsultarPagoServicioUseCase;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.shared.domain.exception.ForbiddenException;

import java.util.Optional;
import java.util.UUID;

public class ConsultarPagoServicioService implements ConsultarPagoServicioUseCase {
    private final PagoServicioRepositoryPort repository;
    private final SolicitudServicioRepositoryPort solicitudRepository;
    private final AsignacionServicioRepositoryPort asignacionRepository;

    public ConsultarPagoServicioService(PagoServicioRepositoryPort repository,
                                        SolicitudServicioRepositoryPort solicitudRepository,
                                        AsignacionServicioRepositoryPort asignacionRepository) {
        this.repository = repository;
        this.solicitudRepository = solicitudRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    public Optional<PagoServicioResult> obtenerEstado(UUID solicitudId, UUID asignacionServicioId,
                                                      UUID encuentroId, UUID usuarioId) {
        if (solicitudId == null || asignacionServicioId == null || usuarioId == null) {
            throw new IllegalArgumentException("solicitudId, asignacionServicioId y usuarioId son obligatorios");
        }
        SolicitudServicio solicitud = solicitudRepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        AsignacionServicio asignacion = asignacionRepository.buscarPorId(asignacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada"));
        if (!asignacion.correspondeASolicitud(solicitudId)) {
            throw new IllegalArgumentException("La asignacion no corresponde a la solicitud");
        }
        boolean participante = usuarioId.equals(solicitud.getSolicitanteId())
                || usuarioId.equals(asignacion.getPrestadorId());
        if (!participante) throw new ForbiddenException("El usuario no participa de este servicio");
        return repository.buscarPorObjetivo(asignacionServicioId, encuentroId)
                .filter(pago -> pago.correspondeA(solicitudId, asignacionServicioId, encuentroId))
                .map(PagoServicioResult::desde);
    }
}
