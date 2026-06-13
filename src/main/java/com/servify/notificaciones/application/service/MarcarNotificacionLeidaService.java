package com.servify.notificaciones.application.service;

import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;
import com.servify.notificaciones.application.port.in.MarcarNotificacionLeidaUseCase;
import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;
import com.servify.shared.domain.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarcarNotificacionLeidaService implements MarcarNotificacionLeidaUseCase {

    private final NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort;

    public MarcarNotificacionLeidaService(NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort) {
        this.notificacionUsuarioRepositoryPort = notificacionUsuarioRepositoryPort;
    }

    @Override
    public NotificacionUsuarioResult marcarLeida(UUID usuarioId, UUID notificacionId) {
        if (usuarioId == null || notificacionId == null) {
            throw new IllegalArgumentException("El usuario y la notificacion son obligatorios");
        }
        var notificacion = notificacionUsuarioRepositoryPort.buscarPorId(notificacionId)
                .orElseThrow(() -> new NotFoundException("La notificacion no existe"));
        if (!usuarioId.equals(notificacion.getUsuarioId())) {
            throw new NotFoundException("La notificacion no existe para este usuario");
        }
        notificacion.marcarLeida(LocalDateTime.now());
        return CrearNotificacionUsuarioService.toResult(notificacionUsuarioRepositoryPort.guardar(notificacion));
    }
}
