package com.servify.publicaciones.application.dto;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de comando para actualizar los datos de una publicacion de servicio.
 */
public class ActualizarPublicacionCommand {

    private UUID publicacionServicioId;
    private UUID usuarioId;
    private UUID categoriaServicioId;
    private String titulo;
    private String descripcion;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private List<Ubicacion> zonasCobertura;
    private List<DisponibilidadHoraria> disponibilidadesHorarias;
    private BigDecimal precioBase;

    public ActualizarPublicacionCommand() {
    }

    public ActualizarPublicacionCommand(UUID publicacionServicioId,
                                        UUID usuarioId,
                                        UUID categoriaServicioId,
                                        String titulo,
                                        String descripcion,
                                        ModalidadServicio modalidadServicio,
                                        Ubicacion ubicacion,
                                        List<DisponibilidadHoraria> disponibilidadesHorarias,
                                        BigDecimal precioBase) {
        this(
                publicacionServicioId,
                usuarioId,
                categoriaServicioId,
                titulo,
                descripcion,
                modalidadServicio,
                ubicacion,
                ubicacion == null ? List.of() : List.of(ubicacion),
                disponibilidadesHorarias,
                precioBase
        );
    }

    public ActualizarPublicacionCommand(UUID publicacionServicioId,
                                        UUID usuarioId,
                                        UUID categoriaServicioId,
                                        String titulo,
                                        String descripcion,
                                        ModalidadServicio modalidadServicio,
                                        Ubicacion ubicacion,
                                        List<Ubicacion> zonasCobertura,
                                        List<DisponibilidadHoraria> disponibilidadesHorarias,
                                        BigDecimal precioBase) {
        this.publicacionServicioId = publicacionServicioId;
        this.usuarioId = usuarioId;
        this.categoriaServicioId = categoriaServicioId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.modalidadServicio = modalidadServicio;
        this.ubicacion = ubicacion;
        this.zonasCobertura = zonasCobertura;
        this.disponibilidadesHorarias = disponibilidadesHorarias;
        this.precioBase = precioBase;
    }

    public UUID getPublicacionServicioId() {
        return publicacionServicioId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getCategoriaServicioId() {
        return categoriaServicioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ModalidadServicio getModalidadServicio() {
        return modalidadServicio;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public List<Ubicacion> getZonasCobertura() {
        return zonasCobertura;
    }

    public List<DisponibilidadHoraria> getDisponibilidadesHorarias() {
        return disponibilidadesHorarias;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }
}
