package com.servify.autenticacion.infrastructure.mail;

import com.servify.autenticacion.application.port.out.PasswordResetEmailSenderPort;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailSenderAdapter implements PasswordResetEmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailSenderAdapter.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailHost;
    private final String from;

    public PasswordResetEmailSenderAdapter(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${servify.mail.from:servifycommunity@gmail.com}") String from
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailHost = mailHost;
        this.from = from;
    }

    @Override
    public void enviarLinkRecuperacion(String email, String resetLink, LocalDateTime fechaExpiracion) {
        if (email == null || email.isBlank() || resetLink == null || resetLink.isBlank()) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailHost == null || mailHost.isBlank() || mailSender == null) {
            log.info("Password reset link for {}: {}", email, resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject("Recupera tu cuenta de Servify");
            message.setText(buildBody(resetLink, fechaExpiracion));
            mailSender.send(message);
        } catch (RuntimeException exception) {
            log.warn("No se pudo enviar el email de recuperacion a {}. Link: {}", email, resetLink, exception);
        }
    }

    private String buildBody(String resetLink, LocalDateTime fechaExpiracion) {
        return """
                Recibimos una solicitud para recuperar tu cuenta de Servify.

                Para crear una nueva contrasena, abri este enlace:
                %s

                El enlace vence el %s y solo puede usarse una vez.
                Si no pediste este cambio, podes ignorar este mensaje.

                Equipo Servify
                """.formatted(resetLink, fechaExpiracion);
    }
}
