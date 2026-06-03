package com.servify.publicaciones.application.dto;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de consulta para buscar publicaciones compatibles con un criterio.
 */
public class BuscarPublicacionesCompatiblesQuery {

    private UUID solicitudServicioId;
    private UUID categoriaServicioId;
    private String descripcionNecesidad;
    private ModalidadServicio modalidadRequerida;
    private Ubicacion ubicacionRequerida;
    private DisponibilidadHoraria disponibilidadRequerida;
    private BigDecimal precioMaximo;
    private Double radioBusquedaKm;

    public BuscarPublicacionesCompatiblesQuery() {
    }

    public BuscarPublicacionesCompatiblesQuery(UUID solicitudServicioId,
                                               UUID categoriaServicioId,
                                               String descripcionNecesidad,
                                               ModalidadServicio modalidadRequerida,
                                               Ubicacion ubicacionRequerida,
                                               DisponibilidadHoraria disponibilidadRequerida,
                                               BigDecimal precioMaximo,
                                               Double radioBusquedaKm) {
        this.solicitudServicioId = solicitudServicioId;
        this.categoriaServicioId = categoriaServicioId;
        this.descripcionNecesidad = descripcionNecesidad;
        this.modalidadRequerida = modalidadRequerida;
        this.ubicacionRequerida = ubicacionRequerida;
        this.disponibilidadRequerida = disponibilidadRequerida;
        this.precioMaximo = precioMaximo;
        this.radioBusquedaKm = radioBusquedaKm;
    }

    public UUID getSolicitudServicioId() {
        return solicitudServicioId;
    }

    public UUID getCategoriaServicioId() {
        return categoriaServicioId;
    }

    public String getDescripcionNecesidad() {
        return descripcionNecesidad;
    }

    public ModalidadServicio getModalidadRequerida() {
        return modalidadRequerida;
    }

    public Ubicacion getUbicacionRequerida() {
        return ubicacionRequerida;
    }

    public DisponibilidadHoraria getDisponibilidadRequerida() {
        return disponibilidadRequerida;
    }

    public BigDecimal getPrecioMaximo() {
        return precioMaximo;
    }

    public Double getRadioBusquedaKm() {
        return radioBusquedaKm;
    }
}
