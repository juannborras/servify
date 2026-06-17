package com.servify.autenticacion.application.dto;

public class RestablecerPasswordCommand {

    private final String token;
    private final String nuevaPassword;

    public RestablecerPasswordCommand(String token, String nuevaPassword) {
        this.token = token;
        this.nuevaPassword = nuevaPassword;
    }

    public String getToken() {
        return token;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }
}
