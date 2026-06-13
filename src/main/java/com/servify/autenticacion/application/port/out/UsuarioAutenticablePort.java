package com.servify.autenticacion.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioAutenticablePort {

    boolean existeUsuario(UUID usuarioId);

    boolean puedeAutenticarse(UUID usuarioId);

    boolean coincideEmailPrincipal(UUID usuarioId, String email);

    Optional<UUID> buscarUsuarioIdPorNombreUsuario(String nombreUsuario);
}
