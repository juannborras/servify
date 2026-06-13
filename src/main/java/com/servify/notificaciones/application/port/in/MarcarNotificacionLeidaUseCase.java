package com.servify.notificaciones.application.port.in;

import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;

import java.util.UUID;

public interface MarcarNotificacionLeidaUseCase {

    NotificacionUsuarioResult marcarLeida(UUID usuarioId, UUID notificacionId);
}
