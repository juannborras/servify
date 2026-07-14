package com.servify.solicitudes.application.service;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.DisponibilidadHorariaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.UbicacionSolicitudResult;
import com.servify.solicitudes.application.port.in.ObtenerSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.util.Optional;
import java.util.UUID;

public class ObtenerSolicitudServicioService implements ObtenerSolicitudServicioUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;

    public ObtenerSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
    }

    @Override
    /**
     * Recupera una solicitud por su identificador.
     * - Valida que `solicitudId` no sea nulo.
     * - Consulta el repositorio y, si existe, mapea la entidad a DTO.
     * - Retorna `Optional.empty()` cuando no existe.
     */
    public Optional<SolicitudServicioResult> obtenerPorId(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }

        Optional<SolicitudServicio> opt = solicitudServicioRepositoryPort.buscarPorId(solicitudId);
        return opt.map(this::construirResultado);
    }

    /**
     * Mapea la entidad `SolicitudServicio` a `SolicitudServicioResult`.
     * Incluye mapeo de `Ubicacion` y `DisponibilidadHoraria` mediante helpers.
     */
    protected SolicitudServicioResult construirResultado(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("solicitudServicio no puede ser nulo");
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

    /**
     * Convierte el Value Object `Ubicacion` a su DTO `UbicacionSolicitudResult`.
     * Devuelve `null` si la ubicación es nula (flujo opcional).
     */
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

    /**
     * Convierte el Value Object `DisponibilidadHoraria` a su DTO `DisponibilidadHorariaResult`.
     * Devuelve `null` si la disponibilidad es nula.
     */
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
