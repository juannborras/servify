package com.servify.notificaciones.application.service;

import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;
import com.servify.notificaciones.domain.model.NotificacionUsuario;

import java.time.LocalDateTime;
import java.util.UUID;

public class CrearNotificacionUsuarioService implements CrearNotificacionUsuarioUseCase {

    private final NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort;

    public CrearNotificacionUsuarioService(NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort) {
        this.notificacionUsuarioRepositoryPort = notificacionUsuarioRepositoryPort;
    }

    @Override
    public NotificacionUsuarioResult crear(CrearNotificacionUsuarioCommand command) {
        if (command == null || command.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario notificado es obligatorio");
        }
        if (command.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de notificacion es obligatorio");
        }
        if (command.getTitulo() == null || command.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El titulo de la notificacion es obligatorio");
        }
        if (command.getMensaje() == null || command.getMensaje().trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje de la notificacion es obligatorio");
        }

        NotificacionUsuario notificacion = new NotificacionUsuario(
                UUID.randomUUID(),
                command.getUsuarioId(),
                command.getTipo(),
                command.getTitulo().trim(),
                command.getMensaje().trim(),
                command.getReferenciaTipo(),
                command.getReferenciaId(),
                false,
                LocalDateTime.now(),
                null
        );
        return toResult(notificacionUsuarioRepositoryPort.guardar(notificacion));
    }

    static NotificacionUsuarioResult toResult(NotificacionUsuario notificacion) {
        return NotificacionUsuarioResult.builder()
                .id(notificacion.getId())
                .usuarioId(notificacion.getUsuarioId())
                .tipo(notificacion.getTipo())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .referenciaTipo(notificacion.getReferenciaTipo())
                .referenciaId(notificacion.getReferenciaId())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaLectura(notificacion.getFechaLectura())
                .build();
    }
}
