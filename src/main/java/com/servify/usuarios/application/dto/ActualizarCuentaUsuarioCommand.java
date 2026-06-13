package com.servify.usuarios.application.dto;

import java.util.UUID;

public class ActualizarCuentaUsuarioCommand {

    private UUID usuarioId;
    private String nombreUsuario;

    public ActualizarCuentaUsuarioCommand() {
    }

    public ActualizarCuentaUsuarioCommand(UUID usuarioId, String nombreUsuario) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}
