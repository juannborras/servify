package com.servify.solicitudes.domain.model;

import com.servify.shared.domain.model.BaseEntity;
import com.servify.solicitudes.domain.enumtype.EstadoRecurrenciaServicio;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class ServicioRecurrencia extends BaseEntity {

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

    protected ServicioRecurrencia() {
    }

    public ServicioRecurrencia(UUID id,
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
        super(id);
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
        validar();
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

    public boolean estaActiva() {
        return estado == EstadoRecurrenciaServicio.ACTIVA;
    }

    public boolean estaCancelada() {
        return estado == EstadoRecurrenciaServicio.CANCELADA;
    }

    public void activar(UUID asignacionServicioId) {
        if (estaCancelada()) {
            throw new IllegalStateException("No se puede activar una recurrencia cancelada");
        }
        if (asignacionServicioId == null) {
            throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        }
        this.asignacionServicioId = asignacionServicioId;
        this.estado = EstadoRecurrenciaServicio.ACTIVA;
    }

    public void cancelar(UUID usuarioId, String motivo, LocalDateTime fechaCancelacion) {
        if (estaCancelada()) {
            throw new IllegalStateException("La recurrencia ya esta cancelada");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
        this.estado = EstadoRecurrenciaServicio.CANCELADA;
        this.canceladaPorId = usuarioId;
        this.motivoCancelacion = motivo;
        this.fechaCancelacion = fechaCancelacion != null ? fechaCancelacion : LocalDateTime.now();
    }

    public void finalizar() {
        if (estaCancelada()) {
            throw new IllegalStateException("No se puede finalizar una recurrencia cancelada");
        }
        this.estado = EstadoRecurrenciaServicio.FINALIZADA;
    }

    private void validar() {
        if (solicitudId == null || frecuencia == null || diaSemana == null || horaDesde == null || horaHasta == null || fechaInicio == null || estado == null) {
            throw new IllegalArgumentException("La recurrencia tiene campos obligatorios incompletos");
        }
        if (!horaDesde.isBefore(horaHasta)) {
            throw new IllegalArgumentException("La hora desde debe ser anterior a la hora hasta");
        }
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio");
        }
    }
}
