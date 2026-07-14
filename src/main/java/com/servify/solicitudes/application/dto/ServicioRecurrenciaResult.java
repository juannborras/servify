package com.servify.solicitudes.application.dto;

import com.servify.solicitudes.domain.enumtype.EstadoRecurrenciaServicio;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class ServicioRecurrenciaResult {

    private UUID id;
    private UUID solicitudId;
    private UUID asignacionServicioId;
    private FrecuenciaRecurrencia frecuencia;
    private DayOfWeek diaSemana;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoRecurrenciaServicio estado;
    private UUID canceladaPorId;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;

    public ServicioRecurrenciaResult() {
    }

    public ServicioRecurrenciaResult(UUID id,
                                     UUID solicitudId,
                                     UUID asignacionServicioId,
                                     FrecuenciaRecurrencia frecuencia,
                                     DayOfWeek diaSemana,
                                     LocalTime horaDesde,
                                     LocalTime horaHasta,
                                     LocalDate fechaInicio,
                                     LocalDate fechaFin,
                                     EstadoRecurrenciaServicio estado,
                                     UUID canceladaPorId,
                                     LocalDateTime fechaCancelacion,
                                     String motivoCancelacion) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.frecuencia = frecuencia;
        this.diaSemana = diaSemana;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.canceladaPorId = canceladaPorId;
        this.fechaCancelacion = fechaCancelacion;
        this.motivoCancelacion = motivoCancelacion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public FrecuenciaRecurrencia getFrecuencia() {
        return frecuencia;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoRecurrenciaServicio getEstado() {
        return estado;
    }

    public UUID getCanceladaPorId() {
        return canceladaPorId;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }
}
