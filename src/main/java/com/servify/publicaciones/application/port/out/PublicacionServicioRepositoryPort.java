package com.servify.publicaciones.application.port.out;

import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia y consultas de publicaciones de servicio.
 */
public interface PublicacionServicioRepositoryPort {

    PublicacionServicio guardar(PublicacionServicio publicacionServicio);

    Optional<PublicacionServicio> buscarPorId(UUID publicacionServicioId);

    List<PublicacionServicio> buscarPorUsuarioId(UUID usuarioId);

    List<PublicacionServicio> buscarActivas();

    default List<PublicacionServicio> buscarPorEstado(EstadoPublicacion estado) {
        if (EstadoPublicacion.ACTIVA.equals(estado)) {
            return buscarActivas();
        }
        return List.of();
    }

    List<PublicacionServicio> buscarActivasPorCategoria(UUID categoriaServicioId);

    boolean existePorUsuarioIdYTitulo(UUID usuarioId, String titulo);

    default boolean existePorUsuarioIdTituloYLocalidad(UUID usuarioId, String titulo, String localidad) {
        return existePorUsuarioIdYTitulo(usuarioId, titulo);
    }
}
