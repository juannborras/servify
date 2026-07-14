package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;

final class ServicioAgendaMapper {

    private ServicioAgendaMapper() {
    }

    static ServicioEncuentroResult toEncuentroResult(ServicioEncuentro encuentro) {
        if (encuentro == null) {
            return null;
        }
        return new ServicioEncuentroResult(
                encuentro.getId(),
                encuentro.getSolicitudId(),
                encuentro.getAsignacionServicioId(),
                encuentro.getRecurrenciaServicioId(),
                encuentro.getPropuestoPorId(),
                encuentro.getFechaInicio(),
                encuentro.getFechaFin(),
                encuentro.getEstado(),
                encuentro.getMensaje(),
                encuentro.getFechaResolucion()
        );
    }

    static ServicioRecurrenciaResult toRecurrenciaResult(ServicioRecurrencia recurrencia) {
        if (recurrencia == null) {
            return null;
        }
        return new ServicioRecurrenciaResult(
                recurrencia.getId(),
                recurrencia.getSolicitudId(),
                recurrencia.getAsignacionServicioId(),
                recurrencia.getFrecuencia(),
                recurrencia.getDiaSemana(),
                recurrencia.getHoraDesde(),
                recurrencia.getHoraHasta(),
                recurrencia.getFechaInicio(),
                recurrencia.getFechaFin(),
                recurrencia.getEstado(),
                recurrencia.getCanceladaPorId(),
                recurrencia.getFechaCancelacion(),
                recurrencia.getMotivoCancelacion()
        );
    }
}
