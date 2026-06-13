package com.servify.notificaciones.application.port.in;

import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;

import java.util.List;
import java.util.UUID;

public interface ListarNotificacionesUsuarioUseCase {

    List<NotificacionUsuarioResult> listarPorUsuarioId(UUID usuarioId);
}
