package com.servify.autenticacion.application.service;

import com.servify.autenticacion.application.dto.IniciarSesionCommand;
import com.servify.autenticacion.application.dto.SesionResult;
import com.servify.autenticacion.application.dto.TokenResult;
import com.servify.autenticacion.application.port.in.IniciarSesionUseCase;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.autenticacion.domain.model.CredencialAcceso;
import com.servify.autenticacion.domain.model.RefreshToken;
import java.time.LocalDateTime;
import java.util.UUID;

public class IniciarSesionService implements IniciarSesionUseCase {

    private static final String MENSAJE_CUENTA_INEXISTENTE =
            "No existe una cuenta registrada con ese email o nombre de usuario";

    private final CredencialAccesoRepositoryPort credencialAccesoRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UsuarioAutenticablePort usuarioAutenticablePort;

    public IniciarSesionService(CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
                                PasswordHasherPort passwordHasherPort,
                                TokenProviderPort tokenProviderPort,
                                RefreshTokenRepositoryPort refreshTokenRepositoryPort,
                                UsuarioAutenticablePort usuarioAutenticablePort) {
        this.credencialAccesoRepositoryPort = credencialAccesoRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.usuarioAutenticablePort = usuarioAutenticablePort;
    }

    @Override
    public SesionResult iniciar(IniciarSesionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getEmailAcceso() == null || command.getEmailAcceso().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu email o nombre de usuario");
        }
        if (command.getPasswordPlano() == null || command.getPasswordPlano().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu contrasena");
        }

        CredencialAcceso credencial = obtenerCredencial(command.getEmailAcceso());
        if (!credencial.estaHabilitada()) {
            throw new IllegalStateException("La credencial no esta habilitada");
        }
        if (!usuarioAutenticablePort.puedeAutenticarse(credencial.getUsuarioId())) {
            throw new IllegalStateException("El usuario no puede autenticarse");
        }

        LocalDateTime ahora = obtenerFechaActual();
        try {
            validarCredencialYPassword(credencial, command.getPasswordPlano());
        } catch (RuntimeException exception) {
            this.credencialAccesoRepositoryPort.guardar(credencial);
            throw exception;
        }
        this.credencialAccesoRepositoryPort.guardar(credencial);

        TokenResult access = tokenProviderPort.generarAccessToken(credencial.getUsuarioId(), credencial.getEmailAcceso());
        TokenResult refresh = tokenProviderPort.generarRefreshToken(credencial.getUsuarioId(), credencial.getEmailAcceso());

        RefreshToken refreshDominio = construirRefreshToken(credencial, refresh);
        this.refreshTokenRepositoryPort.guardar(refreshDominio);

        return construirResultado(credencial, access, refresh, ahora);
    }

    protected CredencialAcceso obtenerCredencial(String identificadorAcceso) {
        if (identificadorAcceso == null || identificadorAcceso.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu email o nombre de usuario");
        }

        String identificadorNormalizado = identificadorAcceso.trim();
        if (identificadorNormalizado.contains("@")) {
            return this.credencialAccesoRepositoryPort.buscarPorEmailAcceso(identificadorNormalizado)
                    .orElseThrow(() -> new IllegalArgumentException(MENSAJE_CUENTA_INEXISTENTE));
        }

        UUID usuarioId = usuarioAutenticablePort.buscarUsuarioIdPorNombreUsuario(identificadorNormalizado)
                .orElseThrow(() -> new IllegalArgumentException(MENSAJE_CUENTA_INEXISTENTE));
        return this.credencialAccesoRepositoryPort.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(MENSAJE_CUENTA_INEXISTENTE));
    }

    protected void validarCredencialYPassword(CredencialAcceso credencialAcceso,
                                              String passwordPlano) {
        if (credencialAcceso == null) {
            throw new IllegalArgumentException("Credencial no puede ser nula");
        }
        if (passwordPlano == null) {
            throw new IllegalArgumentException("Ingresa tu contrasena");
        }
        LocalDateTime ahora = obtenerFechaActual();
        boolean coincide = this.passwordHasherPort.coincide(passwordPlano, credencialAcceso.getPasswordHash());
        if (coincide) {
            credencialAcceso.registrarAccesoExitoso(ahora);
        } else {
            credencialAcceso.registrarIntentoFallido();
            throw new IllegalArgumentException("Contrasena incorrecta");
        }
    }

    protected RefreshToken construirRefreshToken(CredencialAcceso credencialAcceso,
                                                 TokenResult refreshToken) {
        if (credencialAcceso == null || refreshToken == null) {
            throw new IllegalArgumentException("credencialAcceso y refreshToken no pueden ser nulos");
        }
        String hash = this.tokenProviderPort.obtenerHashToken(refreshToken.getToken());
        return new RefreshToken(
                generarIdRefreshToken(),
                credencialAcceso.getUsuarioId(),
                credencialAcceso.getId(),
                hash,
                refreshToken.getFechaEmision(),
                refreshToken.getFechaExpiracion(),
                null,
                true
        );
    }

    protected SesionResult construirResultado(CredencialAcceso credencialAcceso,
                                              TokenResult accessToken,
                                              TokenResult refreshToken,
                                              LocalDateTime fechaInicioSesion) {
        return SesionResult.builder()
                .usuarioId(credencialAcceso.getUsuarioId())
                .emailAcceso(credencialAcceso.getEmailAcceso())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .fechaInicioSesion(fechaInicioSesion)
                .build();
    }

    protected UUID generarIdRefreshToken() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }
}
