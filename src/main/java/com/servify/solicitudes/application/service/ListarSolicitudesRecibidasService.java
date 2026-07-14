package com.servify.solicitudes.application.service;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.DisponibilidadHorariaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.UbicacionSolicitudResult;
import com.servify.solicitudes.application.port.in.ListarSolicitudesRecibidasUseCase;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Acá hay un detalle importante: este caso de uso devuelve SolicitudServicioResult, o sea la solicitud general.
 Más adelante, si querés mostrar también el estado individual de la distribución para el prestador,
 ahí convendría crear un DTO más rico, por ejemplo algo tipo: SolicitudRecibidaResult
 que combine:
 - datos de la solicitud
 - estado de la distribución
 - fecha de envío
 - fecha de expiración
 */

public class ListarSolicitudesRecibidasService implements ListarSolicitudesRecibidasUseCase {

    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;

    public ListarSolicitudesRecibidasService(DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                             SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
    }

    @Override
    public List<SolicitudServicioResult> listarPorPrestadorId(UUID prestadorId) {
        // Lista las solicitudes cuyos envíos (distribuciones) recibió un prestador.
        // - Recupera distribuciones activas para el prestador y mapea las solicitudes asociadas.
        if (prestadorId == null) {
            throw new IllegalArgumentException("prestadorId no puede ser nulo");
        }
        List<DistribucionSolicitud> distribuciones = obtenerDistribucionesDelPrestador(prestadorId);
        if (distribuciones == null || distribuciones.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<SolicitudServicio> solicitudes = obtenerSolicitudesAsociadas(distribuciones);
        return construirResultados(solicitudes);
    }

    protected List<DistribucionSolicitud> obtenerDistribucionesDelPrestador(UUID prestadorId) {
        if (prestadorId == null) {
            throw new IllegalArgumentException("prestadorId no puede ser nulo");
        }
        return this.distribucionSolicitudRepositoryPort.buscarPorPrestadorId(prestadorId);
    }

    protected List<SolicitudServicio> obtenerSolicitudesAsociadas(List<DistribucionSolicitud> distribuciones) {
        List<SolicitudServicio> resultados = new ArrayList<>();
        if (distribuciones == null || distribuciones.isEmpty()) {
            return resultados;
        }
        for (DistribucionSolicitud d : distribuciones) {
            if (d == null) continue;
            Optional<SolicitudServicio> opt = obtenerSolicitudPorId(d.getSolicitudId());
            if (opt.isPresent()) {
                SolicitudServicio s = opt.get();
                if (!yaFueAgregada(s.getId(), resultados)) {
                    resultados.add(s);
                }
            }
        }
        return resultados;
    }

    protected Optional<SolicitudServicio> obtenerSolicitudPorId(UUID solicitudId) {
        if (solicitudId == null) {
            return Optional.empty();
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId);
    }

    protected List<SolicitudServicioResult> construirResultados(List<SolicitudServicio> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<SolicitudServicioResult> resultados = new ArrayList<>();
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

    protected boolean yaFueAgregada(UUID solicitudId, List<SolicitudServicio> solicitudes) {
        if (solicitudId == null || solicitudes == null) {
            return false;
        }
        return solicitudes.stream().anyMatch(s -> solicitudId.equals(s.getId()));
    }
}
