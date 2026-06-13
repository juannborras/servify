package com.servify.autenticacion.application.service;

import com.servify.autenticacion.application.dto.RegistrarCredencialesCommand;
import com.servify.autenticacion.application.port.in.RegistrarCredencialesUseCase;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.autenticacion.domain.model.CredencialAcceso;

import java.util.UUID;

public class RegistrarCredencialesService implements RegistrarCredencialesUseCase {

    private final CredencialAccesoRepositoryPort credencialAccesoRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UsuarioAutenticablePort usuarioAutenticablePort;

    public RegistrarCredencialesService(CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
                                        PasswordHasherPort passwordHasherPort,
                                        UsuarioAutenticablePort usuarioAutenticablePort) {
        this.credencialAccesoRepositoryPort = credencialAccesoRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.usuarioAutenticablePort = usuarioAutenticablePort;
    }

    @Override
    public void registrar(RegistrarCredencialesCommand command) {
        // Registra credenciales nuevas para un usuario:
        // - valida existencia del usuario y unicidad del email
        // - hashea la contraseña y persiste la credencial
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getUsuarioId() == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        if (command.getEmailAcceso() == null || command.getEmailAcceso().trim().isEmpty()) {
            throw new IllegalArgumentException("emailAcceso no puede ser nulo o vacío");
        }
        if (command.getPasswordPlano() == null || command.getPasswordPlano().trim().isEmpty()) {
            throw new IllegalArgumentException("password no puede ser nulo o vacío");
        }

        validarUsuario(command.getUsuarioId(), command.getEmailAcceso());

        if (this.credencialAccesoRepositoryPort.existePorEmailAcceso(command.getEmailAcceso())) {
            throw new IllegalArgumentException("Ese email ya tiene un usuario registrado");
        }

        String hash = this.passwordHasherPort.hashear(command.getPasswordPlano());
        CredencialAcceso credencial = construirCredencial(command, hash);
        this.credencialAccesoRepositoryPort.guardar(credencial);
    }

    protected void validarUsuario(UUID usuarioId, String emailAcceso) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        if (!this.usuarioAutenticablePort.existeUsuario(usuarioId)) {
            throw new IllegalArgumentException("Usuario no existe: " + usuarioId);
        }
        // Si la implementación puede validar el email principal, validarlo
        try {
            if (!this.usuarioAutenticablePort.coincideEmailPrincipal(usuarioId, emailAcceso)) {
                throw new IllegalArgumentException("El email no corresponde al usuario");
            }
        } catch (UnsupportedOperationException ignored) {
            // algunos adaptadores podrían no implementar esta verificación; no romper en ese caso
        }
    }

    protected CredencialAcceso construirCredencial(RegistrarCredencialesCommand command,
                                                   String passwordHash) {
        return new CredencialAcceso(
            generarIdCredencial(),
            command.getUsuarioId(),
            command.getEmailAcceso(),
            passwordHash,
            true,
            null,
            0
        );
    }

    protected UUID generarIdCredencial() {
        return UUID.randomUUID();
    }
}
