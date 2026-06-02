package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.CrearPublicacionCommand;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.CrearPublicacionUseCase;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.UsuarioPublicadorPort;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.publicaciones.domain.service.ValidadorDisponibilidadHoraria;

import java.util.UUID;

/**
 * Servicio de aplicacion que ejecuta el caso de uso de crear publicacion.
 */
public class CrearPublicacionService implements CrearPublicacionUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;
    private final CategoriaServicioRepositoryPort categoriaServicioRepositoryPort;
    private final UsuarioPublicadorPort usuarioPublicadorPort;
    private final ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria;

    public CrearPublicacionService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
                                   CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
                                   UsuarioPublicadorPort usuarioPublicadorPort,
                                   ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
        this.categoriaServicioRepositoryPort = categoriaServicioRepositoryPort;
        this.usuarioPublicadorPort = usuarioPublicadorPort;
        this.validadorDisponibilidadHoraria = validadorDisponibilidadHoraria;
    }

    @Override
    public PublicacionServicioResult crear(CrearPublicacionCommand command) {
        // Valida command, usuario, duplicados, categoría, disponibilidades, construye, persiste y retorna
        if (command == null || command.getUsuarioId() == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo.");
        }
        validarUsuarioPuedePublicar(command.getUsuarioId());
        String localidad = command.getUbicacion() != null ? command.getUbicacion().getLocalidad() : null;
        if (publicacionServicioRepositoryPort.existePorUsuarioIdTituloYLocalidad(
                command.getUsuarioId(), command.getTitulo(), localidad)) {
            throw new IllegalStateException("Ya existe una publicación con ese título para esta localidad.");
        }
        CategoriaServicio categoriaServicio = obtenerCategoriaActiva(command.getCategoriaServicioId());
        validarDisponibilidades(command);
        PublicacionServicio publicacion = construirPublicacion(command, categoriaServicio);
        PublicacionServicio publicacionGuardada = publicacionServicioRepositoryPort.guardar(publicacion);
        return construirResultado(publicacionGuardada);
    }

    // Verifica que el usuario exista y tenga permisos para publicar servicios
    protected void validarUsuarioPuedePublicar(UUID usuarioId) {
        if (!usuarioPublicadorPort.existeUsuario(usuarioId)) {
            throw new IllegalArgumentException("No se encontró el usuario con id: " + usuarioId);
        }
        if (!usuarioPublicadorPort.puedePublicarServicios(usuarioId)) {
            throw new IllegalStateException("El usuario no tiene permisos para publicar servicios.");
        }
    }

    // Busca la categoría por ID y valida que exista y esté activa
    protected CategoriaServicio obtenerCategoriaActiva(UUID categoriaServicioId) {
        CategoriaServicio categoria = categoriaServicioRepositoryPort.buscarPorId(categoriaServicioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la categoría con id: " + categoriaServicioId));
        if (!categoria.estaActiva()) {
            throw new IllegalStateException("La categoría indicada no está activa.");
        }
        return categoria;
    }

    // Delega la validación de disponibilidades en ValidadorDisponibilidadHoraria
    protected void validarDisponibilidades(CrearPublicacionCommand command) {
        if (command.getDisponibilidadesHorarias() == null || command.getDisponibilidadesHorarias().isEmpty()) {
            throw new IllegalArgumentException("Debe informar al menos una disponibilidad horaria.");
        }
        if (!validadorDisponibilidadHoraria.sonValidas(command.getDisponibilidadesHorarias())) {
            throw new IllegalArgumentException("Las disponibilidades horarias informadas no son válidas o tienen superposiciones.");
        }
    }

    // Construye la entidad con estado inicial INACTIVA y los datos del command
    protected PublicacionServicio construirPublicacion(CrearPublicacionCommand command,
                                                       CategoriaServicio categoriaServicio) {
        return new PublicacionServicio(
                generarIdPublicacion(),
                command.getUsuarioId(),
                categoriaServicio,
                command.getTitulo(),
                command.getDescripcion(),
                command.getModalidadServicio(),
                command.getUbicacion(),
                command.getZonasCobertura(),
                command.getDisponibilidadesHorarias(),
                command.getPrecioBase(),
                EstadoPublicacion.INACTIVA
        );
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

    // Genera un identificador único para la nueva publicación
    protected UUID generarIdPublicacion() {
        return UUID.randomUUID();
    }
}
