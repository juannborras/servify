package com.servify.notificaciones.application.service;

import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;
import com.servify.notificaciones.application.port.in.ListarNotificacionesUsuarioUseCase;
import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ListarNotificacionesUsuarioService implements ListarNotificacionesUsuarioUseCase {

    private final NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort;

    public ListarNotificacionesUsuarioService(NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort) {
        this.notificacionUsuarioRepositoryPort = notificacionUsuarioRepositoryPort;
    }

    @Override
    public List<NotificacionUsuarioResult> listarPorUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio");
        }
        return notificacionUsuarioRepositoryPort.buscarPorUsuarioId(usuarioId).stream()
                .sorted(Comparator.comparing(
                        com.servify.notificaciones.domain.model.NotificacionUsuario::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(CrearNotificacionUsuarioService::toResult)
                .toList();
    }
}
