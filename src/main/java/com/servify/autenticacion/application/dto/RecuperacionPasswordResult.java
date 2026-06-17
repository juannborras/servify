package com.servify.autenticacion.application.dto;

import java.time.LocalDateTime;

public class RecuperacionPasswordResult {

    private final String mensaje;
    private final LocalDateTime fechaExpiracion;
    private final String debugToken;

    public RecuperacionPasswordResult(String mensaje, LocalDateTime fechaExpiracion, String debugToken) {
        this.mensaje = mensaje;
        this.fechaExpiracion = fechaExpiracion;
        this.debugToken = debugToken;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public String getDebugToken() {
        return debugToken;
    }
}
