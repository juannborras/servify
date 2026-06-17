package com.servify.publicaciones.application.port.out;

import com.servify.publicaciones.domain.model.CategoriaServicio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia y consultas de categorias de servicio.
 */
public interface CategoriaServicioRepositoryPort {

    CategoriaServicio guardar(CategoriaServicio categoriaServicio);

    Optional<CategoriaServicio> buscarPorId(UUID categoriaServicioId);

    Optional<CategoriaServicio> buscarPorNombre(String nombre);

    List<CategoriaServicio> listarTodas();

    List<CategoriaServicio> listarActivas();

    boolean existePorNombre(String nombre);
}
