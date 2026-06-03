package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;
import com.servify.solicitudes.application.port.out.ConfiguracionDistribucionPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.PublicacionesCompatiblesPort;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.MotorDistribucionSolicitudes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DistribuidorSolicitudService {

    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final PublicacionesCompatiblesPort publicacionesCompatiblesPort;
    private final ConfiguracionDistribucionPort configuracionDistribucionPort;
    private final MotorDistribucionSolicitudes motorDistribucionSolicitudes;

    public DistribuidorSolicitudService(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            PublicacionesCompatiblesPort publicacionesCompatiblesPort,
            ConfiguracionDistribucionPort configuracionDistribucionPort,
            MotorDistribucionSolicitudes motorDistribucionSolicitudes
    ) {
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.publicacionesCompatiblesPort = publicacionesCompatiblesPort;
        this.configuracionDistribucionPort = configuracionDistribucionPort;
        this.motorDistribucionSolicitudes = motorDistribucionSolicitudes;
    }

    public List<DistribucionSolicitud> distribuir(SolicitudServicio solicitud) {
        if (solicitud == null || !motorDistribucionSolicitudes.debeIniciarDistribucion(solicitud)) {
            return List.of();
        }

        List<DistribucionSolicitud> existentes = distribucionSolicitudRepositoryPort.buscarPorSolicitudId(solicitud.getId());
        Integer radioInicialKm = configuracionDistribucionPort.obtenerRadioBusquedaInicialKm();
        Map<UUID, UUID> publicacionesCompatibles = publicacionesCompatiblesPort.buscarPublicacionesCompatibles(
                solicitud.getId(),
                solicitud.getCategoriaServicioId(),
                solicitud.getDescripcionNecesidad(),
                solicitud.getModalidadServicio(),
                solicitud.getUbicacion(),
                solicitud.getDisponibilidadRequerida(),
                solicitud.getPrecioReferencia(),
                radioInicialKm
        );

        LocalDateTime fechaEnvio = LocalDateTime.now();
        LocalDateTime fechaExpiracion = calcularFechaExpiracion(fechaEnvio);
        List<DistribucionSolicitud> realineadas = realinearPendientesConMejorPublicacion(
                publicacionesCompatibles,
                existentes
        );
        List<DistribucionSolicitud> nuevas = motorDistribucionSolicitudes.crearDistribucionesNuevaRonda(
                solicitud,
                publicacionesCompatibles,
                existentes,
                fechaEnvio,
                fechaExpiracion
        );

        List<DistribucionSolicitud> guardadas = nuevas.stream()
                .map(distribucionSolicitudRepositoryPort::guardar)
                .toList();
        if (realineadas.isEmpty()) {
            return guardadas;
        }
        return java.util.stream.Stream.concat(realineadas.stream(), guardadas.stream()).toList();
    }

    public DistribucionSolicitudResult construirResultado(DistribucionSolicitud distribucion) {
        return DistribucionSolicitudResult.builder()
                .id(distribucion.getId())
                .solicitudId(distribucion.getSolicitudId())
                .publicacionServicioId(distribucion.getPublicacionServicioId())
                .prestadorId(distribucion.getPrestadorId())
                .estado(distribucion.getEstado())
                .rondaDistribucion(distribucion.getRondaDistribucion())
                .fechaEnvio(distribucion.getFechaEnvio())
                .fechaRespuesta(distribucion.getFechaRespuesta())
                .fechaExpiracion(distribucion.getFechaExpiracion())
                .build();
    }

    private LocalDateTime calcularFechaExpiracion(LocalDateTime fechaEnvio) {
        if (fechaEnvio == null) {
            return null;
        }
        Integer minutosEspera = configuracionDistribucionPort.obtenerTiempoEsperaExpansionMinutos();
        if (minutosEspera == null || minutosEspera <= 0) {
            return null;
        }
        return fechaEnvio.plusMinutes(minutosEspera);
    }

    private List<DistribucionSolicitud> realinearPendientesConMejorPublicacion(
            Map<UUID, UUID> publicacionesCompatibles,
            List<DistribucionSolicitud> existentes
    ) {
        if (publicacionesCompatibles == null || publicacionesCompatibles.isEmpty()
                || existentes == null || existentes.isEmpty()) {
            return List.of();
        }

        List<DistribucionSolicitud> realineadas = new java.util.ArrayList<>();
        java.util.Set<UUID> prestadoresRealineados = new java.util.HashSet<>();
        for (Map.Entry<UUID, UUID> candidata : publicacionesCompatibles.entrySet()) {
            UUID publicacionId = candidata.getKey();
            UUID prestadorId = candidata.getValue();
            if (!prestadoresRealineados.add(prestadorId)) {
                continue;
            }
            existentes.stream()
                    .filter(distribucion -> distribucion != null
                            && distribucion.estaEnviada()
                            && distribucion.getPrestadorId().equals(prestadorId)
                            && !distribucion.getPublicacionServicioId().equals(publicacionId))
                    .findFirst()
                    .ifPresent(distribucion -> {
                        distribucion.reasignarPublicacion(publicacionId);
                        realineadas.add(distribucionSolicitudRepositoryPort.guardar(distribucion));
                    });
        }
        return realineadas;
    }
}
