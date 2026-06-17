package com.servify.notificaciones.application.service;

import com.servify.notificaciones.application.port.in.EliminarNotificacionUsuarioUseCase;
import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;
import com.servify.shared.domain.exception.NotFoundException;

import java.util.UUID;

public class EliminarNotificacionUsuarioService implements EliminarNotificacionUsuarioUseCase {

    private final NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort;

    public EliminarNotificacionUsuarioService(NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort) {
        this.notificacionUsuarioRepositoryPort = notificacionUsuarioRepositoryPort;
    }

    @Override
    public void eliminar(UUID usuarioId, UUID notificacionId) {
        if (usuarioId == null || notificacionId == null) {
            throw new IllegalArgumentException("El usuario y la notificacion son obligatorios");
        }
        var notificacion = notificacionUsuarioRepositoryPort.buscarPorId(notificacionId)
                .orElseThrow(() -> new NotFoundException("La notificacion no existe"));
        if (!usuarioId.equals(notificacion.getUsuarioId())) {
            throw new NotFoundException("La notificacion no existe para este usuario");
        }
        notificacionUsuarioRepositoryPort.eliminarPorId(notificacionId);
    }
}
