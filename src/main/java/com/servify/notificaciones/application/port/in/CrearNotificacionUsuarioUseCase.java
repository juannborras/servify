package com.servify.notificaciones.application.port.in;

import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;

public interface CrearNotificacionUsuarioUseCase {

    NotificacionUsuarioResult crear(CrearNotificacionUsuarioCommand command);
}
