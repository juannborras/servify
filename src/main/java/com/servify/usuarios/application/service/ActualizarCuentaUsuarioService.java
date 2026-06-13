package com.servify.usuarios.application.service;

import com.servify.shared.domain.exception.BusinessRuleException;
import com.servify.shared.domain.exception.NotFoundException;
import com.servify.shared.domain.exception.ValidationException;
import com.servify.usuarios.application.dto.ActualizarCuentaUsuarioCommand;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.ActualizarCuentaUsuarioUseCase;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.Contacto;
import java.util.Locale;

public class ActualizarCuentaUsuarioService implements ActualizarCuentaUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ActualizarCuentaUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public UsuarioResult actualizar(ActualizarCuentaUsuarioCommand command) {
        if (command == null || command.getUsuarioId() == null) {
            throw new ValidationException("El usuarioId es obligatorio");
        }

        Usuario usuario = usuarioRepositoryPort.buscarPorId(command.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        String nombreUsuario = normalizarNombreUsuario(command.getNombreUsuario());

        if (!nombreUsuario.equals(usuario.getNombreUsuario())
                && usuarioRepositoryPort.existePorNombreUsuario(nombreUsuario)) {
            throw new BusinessRuleException("Ese nombre de usuario ya esta en uso");
        }

        usuario.actualizarNombreUsuario(nombreUsuario);
        Usuario usuarioPersistido = usuarioRepositoryPort.guardar(usuario);
        return construirResultado(usuarioPersistido);
    }

    private UsuarioResult construirResultado(Usuario usuario) {
        Contacto contacto = usuario.getContacto();
        return new UsuarioResult(
                usuario.getId(),
                contacto != null ? contacto.getEmail() : null,
                usuario.getNombreUsuario(),
                contacto != null ? contacto.getTelefono() : null,
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getEstadoValidacionIdentidad(),
                usuario.getFechaRegistro()
        );
    }

    private String normalizarNombreUsuario(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException("El nombre de usuario es obligatorio");
        }
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        if (!normalizado.matches("^[a-z0-9._-]{3,30}$")) {
            throw new ValidationException("El nombre de usuario debe tener 3 a 30 caracteres y solo puede usar letras, numeros, punto, guion o guion bajo");
        }
        return normalizado;
    }
}
