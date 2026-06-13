package com.servify.usuarios.application.service;

import com.servify.shared.domain.exception.ValidationException;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.ListarUsuariosUseCase;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.Contacto;
import java.util.List;

public class ListarUsuariosService implements ListarUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ListarUsuariosService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public List<UsuarioResult> listarPorEstado(EstadoUsuario estado) {
        if (estado == null) {
            throw new ValidationException("El estado de usuario es obligatorio");
        }
        return usuarioRepositoryPort.listarPorEstado(estado).stream()
                .map(this::construirResultado)
                .toList();
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
}
