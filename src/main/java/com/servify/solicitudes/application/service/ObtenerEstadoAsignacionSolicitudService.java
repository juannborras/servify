package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.dto.ContraofertaResult;
import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;
import com.servify.solicitudes.application.dto.EstadoAsignacionSolicitudResult;
import com.servify.solicitudes.application.port.in.ObtenerEstadoAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ContraofertaRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.Contraoferta;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;
import java.util.Objects;

public class ObtenerEstadoAsignacionSolicitudService implements ObtenerEstadoAsignacionSolicitudUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final ContraofertaRepositoryPort contraofertaRepositoryPort;
    private final ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort;
    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;

    public ObtenerEstadoAsignacionSolicitudService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                                   DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                                   AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                                   ContraofertaRepositoryPort contraofertaRepositoryPort,
                                                   ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort) {
        this(solicitudServicioRepositoryPort, distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort, contraofertaRepositoryPort,
                confirmacionFinalizacionRepositoryPort, null, null);
    }

    public ObtenerEstadoAsignacionSolicitudService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                                   DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                                   AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                                   ContraofertaRepositoryPort contraofertaRepositoryPort,
                                                   ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
                                                   ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                                   ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.contraofertaRepositoryPort = contraofertaRepositoryPort;
        this.confirmacionFinalizacionRepositoryPort = confirmacionFinalizacionRepositoryPort;
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
    }

    @Override
    public EstadoAsignacionSolicitudResult obtenerEstado(UUID solicitudId) {
        // Obtiene el estado de asignación de una solicitud:
        // - solicitud principal
        // - asignación si existe
        // - distribuciones activas y contraofertas pendientes
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        SolicitudServicio solicitud = obtenerSolicitudExistente(solicitudId);
        Optional<AsignacionServicio> asignacion = obtenerAsignacion(solicitudId);
        List<DistribucionSolicitud> distribucionesActivas = obtenerDistribucionesActivas(solicitudId);
        List<DistribucionSolicitud> distribucionesAceptadas = obtenerDistribucionesAceptadas(distribucionesActivas);
        List<Contraoferta> contraofertasPendientes = obtenerContraofertasPendientes(distribucionesActivas);
        Optional<ServicioEncuentro> encuentroActivo = asignacion
                .flatMap(a -> obtenerEncuentroRecurrenteActivo(solicitud, a));
        List<ConfirmacionFinalizacion> confirmaciones = encuentroActivo
                .map(encuentro -> confirmacionFinalizacionRepositoryPort.buscarPorEncuentroServicioId(encuentro.getId()))
                .or(() -> asignacion.map(a -> obtenerConfirmacionesDeAsignacion(a.getId())))
                .orElseGet(java.util.Collections::emptyList);

        return construirResultado(
                solicitud,
                asignacion,
                contraofertasPendientes,
                distribucionesAceptadas,
                confirmaciones,
                distribucionesActivas == null ? 0 : distribucionesActivas.size(),
                encuentroActivo.map(ServicioEncuentro::getId).orElse(null)
        );
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected Optional<AsignacionServicio> obtenerAsignacion(UUID solicitudId) {
        if (solicitudId == null) {
            return Optional.empty();
        }
        return this.asignacionServicioRepositoryPort.buscarPorSolicitudId(solicitudId);
    }

    protected List<DistribucionSolicitud> obtenerDistribucionesActivas(UUID solicitudId) {
        if (solicitudId == null) {
            return java.util.Collections.emptyList();
        }
        return this.distribucionSolicitudRepositoryPort.buscarActivasPorSolicitudId(solicitudId);
    }

    protected List<DistribucionSolicitud> obtenerDistribucionesAceptadas(List<DistribucionSolicitud> distribuciones) {
        if (distribuciones == null || distribuciones.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return distribuciones.stream()
                .filter(d -> d != null && d.estaAceptada())
                .toList();
    }

    protected List<ConfirmacionFinalizacion> obtenerConfirmacionesDeAsignacion(UUID asignacionServicioId) {
        if (asignacionServicioId == null) {
            return java.util.Collections.emptyList();
        }
        return this.confirmacionFinalizacionRepositoryPort.buscarPorAsignacionServicioId(asignacionServicioId);
    }

    private Optional<ServicioEncuentro> obtenerEncuentroRecurrenteActivo(SolicitudServicio solicitud,
                                                                         AsignacionServicio asignacion) {
        if (!solicitud.esRecurrente()
                || servicioEncuentroRepositoryPort == null
                || servicioRecurrenciaRepositoryPort == null) {
            return Optional.empty();
        }
        return servicioRecurrenciaRepositoryPort.buscarPorSolicitudId(solicitud.getId())
                .filter(recurrencia -> recurrencia.estaActiva())
                .flatMap(recurrencia -> servicioEncuentroRepositoryPort
                        .buscarPorAsignacionServicioId(asignacion.getId()).stream()
                        .filter(Objects::nonNull)
                        .filter(encuentro -> Objects.equals(encuentro.getRecurrenciaServicioId(), recurrencia.getId()))
                        .filter(ServicioEncuentro::estaConfirmado)
                        .min(Comparator.comparing(ServicioEncuentro::getFechaInicio)));
    }

    protected List<Contraoferta> obtenerContraofertasPendientes(List<DistribucionSolicitud> distribuciones) {
        java.util.List<Contraoferta> resultados = new java.util.ArrayList<>();
        if (distribuciones == null || distribuciones.isEmpty()) {
            return resultados;
        }
        for (DistribucionSolicitud d : distribuciones) {
            if (d == null) continue;
            this.contraofertaRepositoryPort.buscarPendientePorDistribucionSolicitudId(d.getId())
                    .ifPresent(resultados::add);
        }
        return resultados;
    }

    protected AsignacionServicioResult construirAsignacionResult(AsignacionServicio asignacionServicio) {
        if (asignacionServicio == null) {
            return null;
        }
        return AsignacionServicioResult.builder()
            .id(asignacionServicio.getId())
            .solicitudId(asignacionServicio.getSolicitudId())
            .distribucionSolicitudId(asignacionServicio.getDistribucionSolicitudId())
            .prestadorId(asignacionServicio.getPrestadorId())
            .publicacionServicioId(asignacionServicio.getPublicacionServicioId())
            .precioAcordado(asignacionServicio.getPrecioAcordado())
            .estado(asignacionServicio.getEstado())
            .fechaAsignacion(asignacionServicio.getFechaAsignacion())
            .fechaFinalizacion(asignacionServicio.getFechaFinalizacion())
            .build();
    }

    protected DistribucionSolicitudResult construirDistribucionResult(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            return null;
        }
        return DistribucionSolicitudResult.builder()
                .id(distribucionSolicitud.getId())
                .solicitudId(distribucionSolicitud.getSolicitudId())
                .publicacionServicioId(distribucionSolicitud.getPublicacionServicioId())
                .prestadorId(distribucionSolicitud.getPrestadorId())
                .estado(distribucionSolicitud.getEstado())
                .rondaDistribucion(distribucionSolicitud.getRondaDistribucion())
                .fechaEnvio(distribucionSolicitud.getFechaEnvio())
                .fechaRespuesta(distribucionSolicitud.getFechaRespuesta())
                .fechaExpiracion(distribucionSolicitud.getFechaExpiracion())
                .build();
    }

    protected ContraofertaResult construirContraofertaResult(Contraoferta contraoferta) {
        if (contraoferta == null) {
            return null;
        }
        return ContraofertaResult.builder()
                .id(contraoferta.getId())
                .distribucionSolicitudId(contraoferta.getDistribucionSolicitudId())
                .prestadorId(contraoferta.getPrestadorId())
                .precioOriginal(contraoferta.getPrecioOriginal())
                .precioPropuesto(contraoferta.getPrecioPropuesto())
                .mensaje(contraoferta.getMensaje())
                .estado(contraoferta.getEstado())
                .fechaEmision(contraoferta.getFechaEmision())
                .fechaResolucion(contraoferta.getFechaResolucion())
                .build();
    }

    protected EstadoAsignacionSolicitudResult construirResultado(SolicitudServicio solicitudServicio,
                                                                 Optional<AsignacionServicio> asignacionServicio,
                                                                 List<Contraoferta> contraofertasPendientes,
                                                                 List<DistribucionSolicitud> distribucionesAceptadas,
                                                                 List<ConfirmacionFinalizacion> confirmaciones,
                                                                 Integer distribucionesActivas,
                                                                 UUID encuentroActivoId) {
        boolean confirmadoPorSolicitante = existeConfirmacion(confirmaciones, RolConfirmante.SOLICITANTE);
        boolean confirmadoPorPrestador = existeConfirmacion(confirmaciones, RolConfirmante.PRESTADOR);
        boolean asignacionFinalizada = asignacionServicio
                .map(AsignacionServicio::estaFinalizada)
                .orElse(false);
        boolean finalizacionConfirmada = confirmadoPorSolicitante
                && confirmadoPorPrestador
                || asignacionFinalizada
                || solicitudServicio.getEstado() == EstadoSolicitud.FINALIZADA;

        EstadoAsignacionSolicitudResult.Builder builder = EstadoAsignacionSolicitudResult.builder()
                .solicitudId(solicitudServicio.getId())
                .solicitanteId(solicitudServicio.getSolicitanteId())
                .estadoSolicitud(solicitudServicio.getEstado())
                .distribucionesActivas(distribucionesActivas == null ? 0 : distribucionesActivas)
                .confirmadoPorSolicitante(confirmadoPorSolicitante || finalizacionConfirmada)
                .confirmadoPorPrestador(confirmadoPorPrestador || finalizacionConfirmada)
                .finalizacionConfirmada(finalizacionConfirmada)
                .encuentroActivoId(encuentroActivoId);

        asignacionServicio.ifPresent(a -> builder.asignacion(construirAsignacionResult(a)));

        if (distribucionesAceptadas != null && !distribucionesAceptadas.isEmpty()) {
            java.util.List<DistribucionSolicitudResult> resultados = new java.util.ArrayList<>();
            for (DistribucionSolicitud d : distribucionesAceptadas) {
                resultados.add(construirDistribucionResult(d));
            }
            builder.distribucionesAceptadas(resultados);
        }

        if (contraofertasPendientes != null && !contraofertasPendientes.isEmpty()) {
            java.util.List<ContraofertaResult> resultados = new java.util.ArrayList<>();
            for (Contraoferta c : contraofertasPendientes) {
                resultados.add(construirContraofertaResult(c));
            }
            builder.contraofertasPendientes(resultados);
        }

        return builder.build();
    }

    protected boolean existeConfirmacion(List<ConfirmacionFinalizacion> confirmaciones, RolConfirmante rol) {
        if (confirmaciones == null || confirmaciones.isEmpty() || rol == null) {
            return false;
        }
        return confirmaciones.stream()
                .anyMatch(c -> c != null && c.estaConfirmada() && rol == c.getRolConfirmante());
    }
}
