package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.CambiarEstadoPublicacionCommand;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.CambiarEstadoPublicacionUseCase;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.publicaciones.domain.model.PublicacionServicio;

import java.util.UUID;

/**
 * Servicio de aplicacion que ejecuta el cambio de estado de publicacion.
 */
public class CambiarEstadoPublicacionService implements CambiarEstadoPublicacionUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;

    public CambiarEstadoPublicacionService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
    }

    @Override
    public PublicacionServicioResult cambiarEstado(CambiarEstadoPublicacionCommand command) {
        // Valida command, verifica permisos, aplica transición, persiste y retorna resultado
        if (command == null || command.getPublicacionServicioId() == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo.");
        }
        if (command.getEstadoDestino() == null) {
            throw new IllegalArgumentException("El estado destino no puede ser nulo.");
        }
        PublicacionServicio publicacion = obtenerPublicacionExistente(command.getPublicacionServicioId());
        validarCambioPermitido(publicacion, command);
        aplicarEstadoDestino(publicacion, command.getEstadoDestino());
        PublicacionServicio publicacionGuardada = publicacionServicioRepositoryPort.guardar(publicacion);
        return construirResultado(publicacionGuardada);
    }

    // Busca la publicación por ID y lanza excepción si no existe
    protected PublicacionServicio obtenerPublicacionExistente(UUID publicacionServicioId) {
        return publicacionServicioRepositoryPort.buscarPorId(publicacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la publicación con id: " + publicacionServicioId));
    }

    // Valida permisos: BLOQUEADA es solo administrativa, el resto requiere pertenencia
    protected void validarCambioPermitido(PublicacionServicio publicacionServicio,
                                          CambiarEstadoPublicacionCommand command) {
        if (EstadoPublicacion.BLOQUEADA.equals(command.getEstadoDestino())) {
            // Acción administrativa: no requiere validar pertenencia al usuario
            return;
        }
        if (command.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuarioId es requerido para esta operación.");
        }
        if (!publicacionServicio.perteneceA(command.getUsuarioId())) {
            throw new IllegalStateException("La publicación no pertenece al usuario indicado.");
        }
    }

    // Centraliza la transición de estado despachando al método de dominio correspondiente
    protected void aplicarEstadoDestino(PublicacionServicio publicacionServicio,
                                        EstadoPublicacion estadoDestino) {
        switch (estadoDestino) {
            case ACTIVA -> publicacionServicio.activar();
            case INACTIVA -> publicacionServicio.desactivar();
            case PAUSADA -> publicacionServicio.pausar();
            case ELIMINADA -> publicacionServicio.eliminar();
            case BLOQUEADA -> throw new UnsupportedOperationException(
                    "El bloqueo de publicaciones requiere implementación administrativa específica.");
            default -> throw new IllegalArgumentException("Estado destino no soportado: " + estadoDestino);
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
