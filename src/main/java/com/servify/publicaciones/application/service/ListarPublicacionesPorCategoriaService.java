package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.ListarPublicacionesPorCategoriaUseCase;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.model.PublicacionServicio;

import java.util.List;
import java.util.UUID;

public class ListarPublicacionesPorCategoriaService implements ListarPublicacionesPorCategoriaUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;

    public ListarPublicacionesPorCategoriaService(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
    }

    @Override
    public List<PublicacionServicioResult> listarActivasPorCategoria(UUID categoriaServicioId) {
        if (categoriaServicioId == null) {
            throw new IllegalArgumentException("categoriaServicioId no puede ser nulo");
        }
        return publicacionServicioRepositoryPort.buscarActivasPorCategoria(categoriaServicioId)
                .stream()
                .map(this::construirResultado)
                .toList();
    }

    private PublicacionServicioResult construirResultado(PublicacionServicio publicacionServicio) {
        CategoriaServicioResult categoriaResult = CategoriaServicioResult.builder()
                .id(publicacionServicio.getCategoriaServicio().getId())
                .nombre(publicacionServicio.getCategoriaServicio().getNombre())
                .descripcion(publicacionServicio.getCategoriaServicio().getDescripcion())
                .estado(publicacionServicio.getCategoriaServicio().getEstado())
                .fechaCreacion(publicacionServicio.getCategoriaServicio().getFechaCreacion())
                .fechaUltimaModificacion(publicacionServicio.getCategoriaServicio().getFechaUltimaModificacion())
                .build();

        return PublicacionServicioResult.builder()
                .id(publicacionServicio.getId())
                .usuarioId(publicacionServicio.getUsuarioId())
                .categoriaServicio(categoriaResult)
                .titulo(publicacionServicio.getTitulo())
                .descripcion(publicacionServicio.getDescripcion())
                .modalidadServicio(publicacionServicio.getModalidadServicio())
                .ubicacion(publicacionServicio.getUbicacion())
                .zonasCobertura(publicacionServicio.getZonasCobertura())
                .disponibilidadesHorarias(publicacionServicio.getDisponibilidadesHorarias())
                .precioBase(publicacionServicio.getPrecioBase())
                .estado(publicacionServicio.getEstado())
                .puedeParticiparEnDistribucion(publicacionServicio.puedeParticiparEnDistribucion())
                .fechaCreacion(publicacionServicio.getFechaCreacion())
                .fechaUltimaModificacion(publicacionServicio.getFechaUltimaModificacion())
                .build();
    }
}
