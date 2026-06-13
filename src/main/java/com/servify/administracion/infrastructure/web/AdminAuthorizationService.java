package com.servify.administracion.infrastructure.web;

import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.shared.domain.exception.UnauthorizedException;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorizationService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProviderPort tokenProviderPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AdminAuthorizationService(TokenProviderPort tokenProviderPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.tokenProviderPort = tokenProviderPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public Usuario requireAdmin(HttpServletRequest request) {
        Usuario usuario = requireActiveUser(request);

        if (!usuario.esAdmin()) {
            throw new ForbiddenException("Se requiere una cuenta administradora");
        }

        return usuario;
    }

    public Usuario requireActiveUser(HttpServletRequest request) {
        Usuario usuario = requireAuthenticatedUser(request);
        if (!usuario.estaActivo()) {
            throw new ForbiddenException("La cuenta autenticada no esta activa");
        }

        return usuario;
    }

    public Usuario requireAuthenticatedUser(HttpServletRequest request) {
        String token = extraerBearerToken(request);
        var claims = tokenProviderPort.validarAccessToken(token)
                .orElseThrow(() -> new UnauthorizedException("Sesion invalida o expirada"));
        return usuarioRepositoryPort.buscarPorId(claims.getUsuarioId())
                .orElseThrow(() -> new UnauthorizedException("La cuenta autenticada no existe"));
    }

    private String extraerBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Falta el token de autorizacion");
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Falta el token de autorizacion");
        }
        return token;
    }
}
