package com.servify.solicitudes.application.dto;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CrearSolicitudServicioCommand {

    private UUID solicitanteId;
    private UUID categoriaServicioId;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private DisponibilidadHoraria disponibilidadRequerida;
    private String descripcionNecesidad;
    private BigDecimal precioReferencia;
    private TipoProgramacionSolicitud tipoProgramacion;
    private LocalDateTime fechaProgramadaInicio;
    private LocalDateTime fechaProgramadaFin;
    private FrecuenciaRecurrencia frecuenciaRecurrencia;
    private LocalDate fechaInicioRecurrencia;
    private LocalDate fechaFinRecurrencia;

    public CrearSolicitudServicioCommand() {
    }

    public CrearSolicitudServicioCommand(UUID solicitanteId,
                                         UUID categoriaServicioId,
                                         ModalidadServicio modalidadServicio,
                                         Ubicacion ubicacion,
                                         DisponibilidadHoraria disponibilidadRequerida,
                                         String descripcionNecesidad,
                                         BigDecimal precioReferencia) {
        this(solicitanteId, categoriaServicioId, modalidadServicio, ubicacion, disponibilidadRequerida,
                descripcionNecesidad, precioReferencia, TipoProgramacionSolicitud.INMEDIATA, null, null,
                null, null, null);
    }

    public CrearSolicitudServicioCommand(UUID solicitanteId,
                                         UUID categoriaServicioId,
                                         ModalidadServicio modalidadServicio,
                                         Ubicacion ubicacion,
                                         DisponibilidadHoraria disponibilidadRequerida,
                                         String descripcionNecesidad,
                                         BigDecimal precioReferencia,
                                         TipoProgramacionSolicitud tipoProgramacion,
                                         LocalDateTime fechaProgramadaInicio,
                                         LocalDateTime fechaProgramadaFin,
                                         FrecuenciaRecurrencia frecuenciaRecurrencia,
                                         LocalDate fechaInicioRecurrencia,
                                         LocalDate fechaFinRecurrencia) {
        this.solicitanteId = solicitanteId;
        this.categoriaServicioId = categoriaServicioId;
        this.modalidadServicio = modalidadServicio;
        this.ubicacion = ubicacion;
        this.disponibilidadRequerida = disponibilidadRequerida;
        this.descripcionNecesidad = descripcionNecesidad;
        this.precioReferencia = precioReferencia;
        this.tipoProgramacion = tipoProgramacion;
        this.fechaProgramadaInicio = fechaProgramadaInicio;
        this.fechaProgramadaFin = fechaProgramadaFin;
        this.frecuenciaRecurrencia = frecuenciaRecurrencia;
        this.fechaInicioRecurrencia = fechaInicioRecurrencia;
        this.fechaFinRecurrencia = fechaFinRecurrencia;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public UUID getCategoriaServicioId() {
        return categoriaServicioId;
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

    public TipoProgramacionSolicitud getTipoProgramacion() {
        return tipoProgramacion;
    }

    public LocalDateTime getFechaProgramadaInicio() {
        return fechaProgramadaInicio;
    }

    public LocalDateTime getFechaProgramadaFin() {
        return fechaProgramadaFin;
    }

    public FrecuenciaRecurrencia getFrecuenciaRecurrencia() {
        return frecuenciaRecurrencia;
    }

    public LocalDate getFechaInicioRecurrencia() {
        return fechaInicioRecurrencia;
    }

    public LocalDate getFechaFinRecurrencia() {
        return fechaFinRecurrencia;
    }
}
