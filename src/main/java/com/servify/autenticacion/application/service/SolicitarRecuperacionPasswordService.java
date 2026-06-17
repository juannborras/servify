package com.servify.autenticacion.application.service;

import com.servify.autenticacion.application.dto.RecuperacionPasswordResult;
import com.servify.autenticacion.application.dto.SolicitarRecuperacionPasswordCommand;
import com.servify.autenticacion.application.port.in.SolicitarRecuperacionPasswordUseCase;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordResetEmailSenderPort;
import com.servify.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.autenticacion.domain.model.CredencialAcceso;
import com.servify.autenticacion.domain.model.PasswordResetToken;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class SolicitarRecuperacionPasswordService implements SolicitarRecuperacionPasswordUseCase {

    private static final String MENSAJE_RESPUESTA =
            "Si existe una cuenta asociada a ese email, enviaremos instrucciones para recuperar la contrasena.";

    private final CredencialAccesoRepositoryPort credencialAccesoRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final PasswordResetEmailSenderPort passwordResetEmailSenderPort;
    private final TokenProviderPort tokenProviderPort;
    private final UsuarioAutenticablePort usuarioAutenticablePort;
    private final SecureRandom secureRandom;
    private final String frontendUrl;
    private final long tokenTtlMinutes;
    private final boolean exposeDebugToken;

    public SolicitarRecuperacionPasswordService(
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
            PasswordResetEmailSenderPort passwordResetEmailSenderPort,
            TokenProviderPort tokenProviderPort,
            UsuarioAutenticablePort usuarioAutenticablePort,
            String frontendUrl,
            long tokenTtlMinutes,
            boolean exposeDebugToken
    ) {
        this.credencialAccesoRepositoryPort = credencialAccesoRepositoryPort;
        this.passwordResetTokenRepositoryPort = passwordResetTokenRepositoryPort;
        this.passwordResetEmailSenderPort = passwordResetEmailSenderPort;
        this.tokenProviderPort = tokenProviderPort;
        this.usuarioAutenticablePort = usuarioAutenticablePort;
        this.frontendUrl = normalizarFrontendUrl(frontendUrl);
        this.tokenTtlMinutes = tokenTtlMinutes > 0 ? tokenTtlMinutes : 30;
        this.exposeDebugToken = exposeDebugToken;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public RecuperacionPasswordResult solicitar(SolicitarRecuperacionPasswordCommand command) {
        String email = normalizarEmail(command != null ? command.getEmail() : null);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaExpiracion = ahora.plusMinutes(tokenTtlMinutes);

        Optional<CredencialAcceso> credencial = credencialAccesoRepositoryPort.buscarPorEmailAcceso(email);
        if (credencial.isEmpty() || !credencial.get().estaHabilitada()
                || !usuarioAutenticablePort.puedeAutenticarse(credencial.get().getUsuarioId())) {
            return new RecuperacionPasswordResult(MENSAJE_RESPUESTA, fechaExpiracion, null);
        }

        String tokenPlano = generarTokenPlano();
        String tokenHash = tokenProviderPort.obtenerHashToken(tokenPlano);

        invalidarTokensVigentes(credencial.get().getUsuarioId(), ahora);

        PasswordResetToken token = new PasswordResetToken(
                UUID.randomUUID(),
                credencial.get().getUsuarioId(),
                credencial.get().getId(),
                email,
                tokenHash,
                ahora,
                fechaExpiracion,
                null,
                false
        );
        passwordResetTokenRepositoryPort.guardar(token);
        passwordResetEmailSenderPort.enviarLinkRecuperacion(email, construirLink(tokenPlano), fechaExpiracion);

        return new RecuperacionPasswordResult(
                MENSAJE_RESPUESTA,
                fechaExpiracion,
                exposeDebugToken ? tokenPlano : null
        );
    }

    private void invalidarTokensVigentes(UUID usuarioId, LocalDateTime ahora) {
        passwordResetTokenRepositoryPort.buscarVigentesPorUsuarioId(usuarioId, ahora)
                .forEach(token -> {
                    token.invalidar(ahora);
                    passwordResetTokenRepositoryPort.guardar(token);
                });
    }

    private String generarTokenPlano() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String construirLink(String tokenPlano) {
        return frontendUrl + "/?resetToken=" + tokenPlano;
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Ingresa el email registrado");
        }
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizado.contains("@") || normalizado.startsWith("@") || normalizado.endsWith("@")) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
        return normalizado;
    }

    private String normalizarFrontendUrl(String value) {
        String normalized = value == null || value.isBlank() ? "http://localhost:5173" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
