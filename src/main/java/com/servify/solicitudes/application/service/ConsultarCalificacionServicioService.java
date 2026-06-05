package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.CalificacionServicioResult;
import com.servify.solicitudes.application.port.in.ConsultarCalificacionServicioUseCase;
import com.servify.solicitudes.application.port.out.CalificacionRepositoryPort;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.model.Calificacion;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ConsultarCalificacionServicioService implements ConsultarCalificacionServicioUseCase {

    private final CalificacionRepositoryPort calificacionRepositoryPort;

    public ConsultarCalificacionServicioService(CalificacionRepositoryPort calificacionRepositoryPort) {
        this.calificacionRepositoryPort = calificacionRepositoryPort;
    }

    @Override
    public Optional<CalificacionServicioResult> obtenerPorAsignacionYRol(
            UUID solicitudId,
            UUID asignacionServicioId,
            RolConfirmante rolCalificador
    ) {
        if (solicitudId == null || asignacionServicioId == null || rolCalificador == null) {
            throw new IllegalArgumentException("solicitudId, asignacionServicioId y rolCalificador son obligatorios");
        }

        return calificacionRepositoryPort
                .buscarPorAsignacionServicioIdYRolCalificador(asignacionServicioId, rolCalificador)
                .filter(calificacion -> Objects.equals(solicitudId, calificacion.getSolicitudId()))
                .map(this::toResult);
    }

    private CalificacionServicioResult toResult(Calificacion calificacion) {
        return CalificacionServicioResult.builder()
                .id(calificacion.getId())
                .solicitudId(calificacion.getSolicitudId())
                .asignacionServicioId(calificacion.getAsignacionServicioId())
                .calificadorId(calificacion.getCalificadorId())
                .calificadoId(calificacion.getCalificadoId())
                .rolCalificador(calificacion.getRolCalificador())
                .puntaje(calificacion.getPuntaje())
                .fechaCalificacion(calificacion.getFechaCalificacion())
                .build();
    }
}
