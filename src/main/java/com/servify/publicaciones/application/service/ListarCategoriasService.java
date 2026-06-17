package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.port.in.ListarCategoriasUseCase;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.domain.model.CategoriaServicio;

import java.util.Comparator;
import java.util.List;

public class ListarCategoriasService implements ListarCategoriasUseCase {

    private final CategoriaServicioRepositoryPort categoriaServicioRepositoryPort;

    public ListarCategoriasService(CategoriaServicioRepositoryPort categoriaServicioRepositoryPort) {
        this.categoriaServicioRepositoryPort = categoriaServicioRepositoryPort;
    }

    @Override
    public List<CategoriaServicioResult> listarTodas() {
        return categoriaServicioRepositoryPort.listarTodas()
                .stream()
                .sorted(Comparator.comparing(CategoriaServicio::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::construirResultado)
                .toList();
    }

    private CategoriaServicioResult construirResultado(CategoriaServicio categoriaServicio) {
        return CategoriaServicioResult.builder()
                .id(categoriaServicio.getId())
                .nombre(categoriaServicio.getNombre())
                .descripcion(categoriaServicio.getDescripcion())
                .estado(categoriaServicio.getEstado())
                .fechaCreacion(categoriaServicio.getFechaCreacion())
                .fechaUltimaModificacion(categoriaServicio.getFechaUltimaModificacion())
                .build();
    }
}
