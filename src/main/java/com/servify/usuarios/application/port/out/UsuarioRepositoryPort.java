package com.servify.usuarios.application.port.out;

import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia y consultas del agregado Usuario.
 * Define el contrato que la infraestructura debe implementar.
 */
public interface UsuarioRepositoryPort {

    /**
     * Guarda o actualiza un usuario y devuelve la instancia persistida.
     */
    Usuario guardar(Usuario usuario);

    /**
     * Busca un usuario por su identificador unico.
     */
    Optional<Usuario> buscarPorId(UUID usuarioId);

    /**
     * Busca un usuario por su email de acceso.
     */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Busca un usuario por su nombre de usuario unico.
     */
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    /**
     * Indica si existe un usuario registrado con el email provisto.
     */
    boolean existePorEmail(String email);

    /**
     * Indica si existe un usuario registrado con el nombre de usuario provisto.
     */
    boolean existePorNombreUsuario(String nombreUsuario);

    /**
     * Lista usuarios por estado para consultas operativas simples.
     */
    List<Usuario> listarPorEstado(EstadoUsuario estado);
}
