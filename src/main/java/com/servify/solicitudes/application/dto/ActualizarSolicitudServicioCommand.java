package com.servify.solicitudes.application.dto;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.util.UUID;

public class ActualizarSolicitudServicioCommand {

    private UUID solicitudId;
    private UUID solicitanteId;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private DisponibilidadHoraria disponibilidadRequerida;
    private String descripcionNecesidad;
    private BigDecimal precioReferencia;

    public ActualizarSolicitudServicioCommand() {
    }

    public ActualizarSolicitudServicioCommand(UUID solicitudId,
                                             UUID solicitanteId,
                                             ModalidadServicio modalidadServicio,
                                             Ubicacion ubicacion,
                                             DisponibilidadHoraria disponibilidadRequerida,
                                             String descripcionNecesidad,
                                             BigDecimal precioReferencia) {
        this.solicitudId = solicitudId;
        this.solicitanteId = solicitanteId;
        this.modalidadServicio = modalidadServicio;
        this.ubicacion = ubicacion;
        this.disponibilidadRequerida = disponibilidadRequerida;
        this.descripcionNecesidad = descripcionNecesidad;
        this.precioReferencia = precioReferencia;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public ModalidadServicio getModalidadServicio() {
        return modalidadServicio;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public DisponibilidadHoraria getDisponibilidadRequerida() {
        return disponibilidadRequerida;
    }

    public String getDescripcionNecesidad() {
        return descripcionNecesidad;
    }

    public BigDecimal getPrecioReferencia() {
        return precioReferencia;
    }
}
