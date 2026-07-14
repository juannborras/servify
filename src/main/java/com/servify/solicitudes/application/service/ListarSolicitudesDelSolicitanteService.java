package com.servify.solicitudes.application.service;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.DisponibilidadHorariaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.UbicacionSolicitudResult;
import com.servify.solicitudes.application.port.in.ListarSolicitudesDelSolicitanteUseCase;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.List;
import java.util.UUID;

public class ListarSolicitudesDelSolicitanteService implements ListarSolicitudesDelSolicitanteUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;

    public ListarSolicitudesDelSolicitanteService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
    }

    @Override
    public List<SolicitudServicioResult> listarPorSolicitanteId(UUID solicitanteId) {
        // Lista las solicitudes creadas por un solicitante.
        // - Valida el id del solicitante.
        // - Recupera las entidades y las mapea a DTOs.
        if (solicitanteId == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        List<SolicitudServicio> solicitudes = this.solicitudServicioRepositoryPort.buscarPorSolicitanteId(solicitanteId);
        if (solicitudes == null || solicitudes.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<SolicitudServicioResult> resultados = new java.util.ArrayList<>();
        for (SolicitudServicio s : solicitudes) {
            resultados.add(construirResultado(s));
        }
        return resultados;
    }

    protected SolicitudServicioResult construirResultado(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            return null;
        }
        return new SolicitudServicioResult(
                solicitudServicio.getId(),
                solicitudServicio.getSolicitanteId(),
                solicitudServicio.getCategoriaServicioId(),
                solicitudServicio.getModalidadServicio(),
                construirUbicacionResult(solicitudServicio.getUbicacion()),
                construirDisponibilidadResult(solicitudServicio.getDisponibilidadRequerida()),
                solicitudServicio.getDescripcionNecesidad(),
                solicitudServicio.getPrecioReferencia(),
                solicitudServicio.getEstado(),
                solicitudServicio.getFechaSolicitud(),
                solicitudServicio.getTipoProgramacion(),
                solicitudServicio.getFechaProgramadaInicio(),
                solicitudServicio.getFechaProgramadaFin()
        );
    }

    protected UbicacionSolicitudResult construirUbicacionResult(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }
        return new UbicacionSolicitudResult(
                ubicacion.getPais(),
                ubicacion.getProvincia(),
                ubicacion.getCiudad(),
                ubicacion.getLocalidad(),
                ubicacion.getCalle(),
                ubicacion.getAltura(),
                ubicacion.getReferencia(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud()
        );
    }

    protected DisponibilidadHorariaResult construirDisponibilidadResult(DisponibilidadHoraria disponibilidadHoraria) {
        if (disponibilidadHoraria == null) {
            return null;
        }
        return new DisponibilidadHorariaResult(
                disponibilidadHoraria.getDiaSemana(),
                disponibilidadHoraria.getHoraDesde(),
                disponibilidadHoraria.getHoraHasta()
        );
    }
}
