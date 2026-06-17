package com.servify.autenticacion.application.service;

import com.servify.autenticacion.application.dto.RestablecerPasswordCommand;
import com.servify.autenticacion.application.port.in.RestablecerPasswordUseCase;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.domain.model.CredencialAcceso;
import com.servify.autenticacion.domain.model.PasswordResetToken;
import java.time.LocalDateTime;

public class RestablecerPasswordService implements RestablecerPasswordUseCase {

    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final CredencialAccesoRepositoryPort credencialAccesoRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenProviderPort tokenProviderPort;

    public RestablecerPasswordService(
            PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenProviderPort tokenProviderPort
    ) {
        this.passwordResetTokenRepositoryPort = passwordResetTokenRepositoryPort;
        this.credencialAccesoRepositoryPort = credencialAccesoRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public void restablecer(RestablecerPasswordCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando es obligatorio");
        }
        String tokenPlano = normalizarToken(command.getToken());
        String nuevaPassword = validarPassword(command.getNuevaPassword());

        LocalDateTime ahora = LocalDateTime.now();
        String tokenHash = tokenProviderPort.obtenerHashToken(tokenPlano);
        PasswordResetToken token = passwordResetTokenRepositoryPort.buscarPorTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("El enlace de recuperacion no es valido o ya expiro"));
        if (!token.estaVigente(ahora)) {
            throw new IllegalArgumentException("El enlace de recuperacion no es valido o ya expiro");
        }

        CredencialAcceso credencial = credencialAccesoRepositoryPort.buscarPorId(token.getCredencialAccesoId())
                .orElseThrow(() -> new IllegalArgumentException("La credencial asociada no existe"));
        credencial.actualizarPasswordHash(passwordHasherPort.hashear(nuevaPassword));
        credencial.reiniciarIntentosFallidos();
        credencial.habilitar();
        token.marcarUsado(ahora);

        credencialAccesoRepositoryPort.guardar(credencial);
        passwordResetTokenRepositoryPort.guardar(token);
    }

    private String normalizarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token de recuperacion es obligatorio");
        }
        return token.trim();
    }

    private String validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Ingresa una contrasena nueva");
        }
        String value = password.trim();
        if (value.length() < 8
                || !value.matches(".*[A-Z].*")
                || !value.matches(".*[a-z].*")
                || !value.matches(".*\\d.*")
                || !value.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener 8 caracteres, mayuscula, minuscula, numero y caracter especial"
            );
        }
        return value;
    }
}
