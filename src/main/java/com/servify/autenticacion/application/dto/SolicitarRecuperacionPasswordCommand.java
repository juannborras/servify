package com.servify.autenticacion.application.dto;

public class SolicitarRecuperacionPasswordCommand {

    private final String email;

    public SolicitarRecuperacionPasswordCommand(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
