package com.servify.autenticacion.application.port.out;

import java.time.LocalDateTime;

public interface PasswordResetEmailSenderPort {

    void enviarLinkRecuperacion(String email, String resetLink, LocalDateTime fechaExpiracion);
}
