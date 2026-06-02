package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.ActualizarPublicacionCommand;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.ActualizarPublicacionUseCase;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.publicaciones.domain.service.ValidadorDisponibilidadHoraria;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicacion que ejecuta el caso de uso de actualizar publicacion.
 */
public class ActualizarPublicacionService implements ActualizarPublicacionUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;
    private final CategoriaServicioRepositoryPort categoriaServicioRepositoryPort;
    private final ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria;

    public ActualizarPublicacionService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
                                        CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
                                        ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
        this.categoriaServicioRepositoryPort = categoriaServicioRepositoryPort;
        this.validadorDisponibilidadHoraria = validadorDisponibilidadHoraria;
    }

    @Override
    public PublicacionServicioResult actualizar(ActualizarPublicacionCommand command) {
        // Valida command, verifica pertenencia, aplica cambios, persiste y retorna resultado
        if (command == null || command.getPublicacionServicioId() == null) {
            throw new IllegalArgumentException("El comando de actualización no puede ser nulo.");
        }
        PublicacionServicio publicacion = obtenerPublicacionExistente(command.getPublicacionServicioId());
        validarPertenencia(publicacion, command.getUsuarioId());
        validarDisponibilidades(command);
        Optional<CategoriaServicio> categoria = obtenerCategoriaSiCorresponde(command.getCategoriaServicioId());
        aplicarActualizaciones(publicacion, command, categoria);
        PublicacionServicio publicacionGuardada = publicacionServicioRepositoryPort.guardar(publicacion);
        return construirResultado(publicacionGuardada);
    }

    // Busca la publicación por ID y lanza excepción si no existe
    protected PublicacionServicio obtenerPublicacionExistente(UUID publicacionServicioId) {
        return publicacionServicioRepositoryPort.buscarPorId(publicacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la publicación con id: " + publicacionServicioId));
    }

    // Verifica que la publicación pertenezca al usuario indicado
    protected void validarPertenencia(PublicacionServicio publicacionServicio, UUID usuarioId) {
        if (!publicacionServicio.perteneceA(usuarioId)) {
            throw new IllegalStateException("La publicación no pertenece al usuario indicado.");
        }
    }

    // Busca la categoría si fue informada, validando que exista y esté activa
    protected Optional<CategoriaServicio> obtenerCategoriaSiCorresponde(UUID categoriaServicioId) {
        if (categoriaServicioId == null) {
            return Optional.empty();
        }
        CategoriaServicio categoria = categoriaServicioRepositoryPort.buscarPorId(categoriaServicioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la categoría con id: " + categoriaServicioId));
        if (!categoria.estaActiva()) {
            throw new IllegalStateException("La categoría indicada no está activa.");
        }
        return Optional.of(categoria);
    }

    // Aplica cada cambio informado invocando los métodos del dominio
    protected void aplicarActualizaciones(PublicacionServicio publicacionServicio,
                                          ActualizarPublicacionCommand command,
                                          Optional<CategoriaServicio> categoriaServicio) {
        if (command.getTitulo() != null && !command.getTitulo().isBlank()) {
            publicacionServicio.actualizarTitulo(command.getTitulo());
        }
        if (command.getDescripcion() != null && !command.getDescripcion().isBlank()) {
            publicacionServicio.actualizarDescripcion(command.getDescripcion());
        }
        categoriaServicio.ifPresent(publicacionServicio::actualizarCategoria);
        if (command.getModalidadServicio() != null) {
            publicacionServicio.actualizarModalidad(command.getModalidadServicio());
        }
        if (command.getUbicacion() != null) {
            publicacionServicio.actualizarUbicacion(command.getUbicacion());
        }
        if (command.getZonasCobertura() != null && !command.getZonasCobertura().isEmpty()) {
            publicacionServicio.actualizarZonasCobertura(command.getZonasCobertura());
        }
        if (command.getDisponibilidadesHorarias() != null && !command.getDisponibilidadesHorarias().isEmpty()) {
            publicacionServicio.actualizarDisponibilidades(command.getDisponibilidadesHorarias());
        }
        if (command.getPrecioBase() != null) {
            publicacionServicio.actualizarPrecioBase(command.getPrecioBase());
        }
    }

    // Valida las disponibilidades horarias si fueron informadas
    protected void validarDisponibilidades(ActualizarPublicacionCommand command) {
        if (command.getDisponibilidadesHorarias() == null || command.getDisponibilidadesHorarias().isEmpty()) {
            return;
        }
        if (!validadorDisponibilidadHoraria.sonValidas(command.getDisponibilidadesHorarias())) {
            throw new IllegalArgumentException("Las disponibilidades horarias informadas no son válidas o tienen superposiciones.");
        }
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
