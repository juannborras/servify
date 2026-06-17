package com.servify.notificaciones.application.port.in;

import java.util.UUID;

public interface EliminarNotificacionUsuarioUseCase {

    void eliminar(UUID usuarioId, UUID notificacionId);
}
