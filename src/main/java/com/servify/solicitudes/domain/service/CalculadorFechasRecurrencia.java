package com.servify.solicitudes.domain.service;

import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Calcula las ocurrencias de una serie sin depender de persistencia.
 * La primera respeta el dia de semana elegido y las siguientes avanzan
 * desde esa fecha segun la frecuencia configurada.
 */
public class CalculadorFechasRecurrencia {

    public Optional<LocalDate> primeraFecha(ServicioRecurrencia recurrencia) {
        if (recurrencia == null || recurrencia.getFechaInicio() == null || recurrencia.getDiaSemana() == null) {
            return Optional.empty();
        }
        LocalDate fecha = recurrencia.getFechaInicio();
        while (fecha.getDayOfWeek() != recurrencia.getDiaSemana()) {
            fecha = fecha.plusDays(1);
        }
        return dentroDelPrograma(recurrencia, fecha) ? Optional.of(fecha) : Optional.empty();
    }

    public Optional<LocalDate> siguienteFecha(ServicioRecurrencia recurrencia, LocalDate fechaActual) {
        if (recurrencia == null || fechaActual == null || recurrencia.getFrecuencia() == null) {
            return Optional.empty();
        }
        LocalDate siguiente = switch (recurrencia.getFrecuencia()) {
            case SEMANAL -> fechaActual.plusWeeks(1);
            case QUINCENAL -> fechaActual.plusWeeks(2);
            case MENSUAL -> fechaActual.plusMonths(1);
        };
        return dentroDelPrograma(recurrencia, siguiente) ? Optional.of(siguiente) : Optional.empty();
    }

    private boolean dentroDelPrograma(ServicioRecurrencia recurrencia, LocalDate fecha) {
        return recurrencia.getFechaFin() == null || !fecha.isAfter(recurrencia.getFechaFin());
    }
}
