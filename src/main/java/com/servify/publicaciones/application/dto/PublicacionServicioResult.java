package com.servify.publicaciones.application.dto;

import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida para exponer datos de una publicacion de servicio.
 */
public class PublicacionServicioResult {

    private UUID id;
    private UUID usuarioId;
    private CategoriaServicioResult categoriaServicio;
    private String titulo;
    private String descripcion;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private List<Ubicacion> zonasCobertura;
    private List<DisponibilidadHoraria> disponibilidadesHorarias;
    private BigDecimal precioBase;
    private EstadoPublicacion estado;
    private Boolean puedeParticiparEnDistribucion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;

    private PublicacionServicioResult() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public CategoriaServicioResult getCategoriaServicio() {
        return categoriaServicio;
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

    public EstadoPublicacion getEstado() {
        return estado;
    }

    public Boolean getPuedeParticiparEnDistribucion() {
        return puedeParticiparEnDistribucion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final PublicacionServicioResult instance;

        public Builder() {
            this.instance = new PublicacionServicioResult();
        }

        public Builder id(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder usuarioId(UUID usuarioId) {
            instance.usuarioId = usuarioId;
            return this;
        }

        public Builder categoriaServicio(CategoriaServicioResult categoriaServicio) {
            instance.categoriaServicio = categoriaServicio;
            return this;
        }

        public Builder titulo(String titulo) {
            instance.titulo = titulo;
            return this;
        }

        public Builder descripcion(String descripcion) {
            instance.descripcion = descripcion;
            return this;
        }

        public Builder modalidadServicio(ModalidadServicio modalidadServicio) {
            instance.modalidadServicio = modalidadServicio;
            return this;
        }

        public Builder ubicacion(Ubicacion ubicacion) {
            instance.ubicacion = ubicacion;
            return this;
        }

        public Builder zonasCobertura(List<Ubicacion> zonasCobertura) {
            instance.zonasCobertura = zonasCobertura;
            return this;
        }

        public Builder disponibilidadesHorarias(List<DisponibilidadHoraria> disponibilidadesHorarias) {
            instance.disponibilidadesHorarias = disponibilidadesHorarias;
            return this;
        }

        public Builder precioBase(BigDecimal precioBase) {
            instance.precioBase = precioBase;
            return this;
        }

        public Builder estado(EstadoPublicacion estado) {
            instance.estado = estado;
            return this;
        }

        public Builder puedeParticiparEnDistribucion(Boolean puedeParticiparEnDistribucion) {
            instance.puedeParticiparEnDistribucion = puedeParticiparEnDistribucion;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            instance.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder fechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
            instance.fechaUltimaModificacion = fechaUltimaModificacion;
            return this;
        }

        public PublicacionServicioResult build() {
            return instance;
        }
    }
}
