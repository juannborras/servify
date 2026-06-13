package com.servify.administracion.application.service;

import com.servify.administracion.application.port.in.ListarPublicacionesAdminUseCase;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;

import java.util.List;

public class ListarPublicacionesAdminService implements ListarPublicacionesAdminUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;

    public ListarPublicacionesAdminService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
    }

    @Override
    public List<PublicacionServicioResult> listarPorEstado(EstadoPublicacion estado) {
        return publicacionServicioRepositoryPort.buscarPorEstado(estado).stream()
                .map(this::construirResultado)
                .toList();
    }

    private PublicacionServicioResult construirResultado(PublicacionServicio publicacionServicio) {
        CategoriaServicio categoria = publicacionServicio.getCategoriaServicio();
        CategoriaServicioResult categoriaResult = categoria == null ? null : CategoriaServicioResult.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .estado(categoria.getEstado())
                .fechaCreacion(categoria.getFechaCreacion())
                .fechaUltimaModificacion(categoria.getFechaUltimaModificacion())
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
