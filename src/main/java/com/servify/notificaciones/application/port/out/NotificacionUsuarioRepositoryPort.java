package com.servify.notificaciones.application.port.out;

import com.servify.notificaciones.domain.model.NotificacionUsuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacionUsuarioRepositoryPort {

    NotificacionUsuario guardar(NotificacionUsuario notificacion);

    Optional<NotificacionUsuario> buscarPorId(UUID notificacionId);

    List<NotificacionUsuario> buscarPorUsuarioId(UUID usuarioId);
}
