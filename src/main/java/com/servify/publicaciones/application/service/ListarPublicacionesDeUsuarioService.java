package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.ListarPublicacionesDeUsuarioUseCase;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.model.PublicacionServicio;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicacion que lista publicaciones de un usuario.
 */
public class ListarPublicacionesDeUsuarioService implements ListarPublicacionesDeUsuarioUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;

    public ListarPublicacionesDeUsuarioService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
    }

    // Valida usuarioId, consulta el repositorio y mapea cada publicación al DTO de salida
    @Override
    public List<PublicacionServicioResult> listarPorUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId no puede ser nulo.");
        }
        return publicacionServicioRepositoryPort.buscarPorUsuarioId(usuarioId)
                .stream()
                .map(this::construirResultado)
                .collect(Collectors.toList());
    }

    // Mapea la entidad de dominio al DTO de salida
    protected PublicacionServicioResult construirResultado(PublicacionServicio publicacionServicio) {
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
