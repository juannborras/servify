package com.servify.solicitudes.domain.service;

import com.servify.solicitudes.domain.enumtype.EstadoDistribucion;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

//se utilizo:
//Map<UUID, UUID> publicacionesPorPrestador
//donde:
//key = publicacionServicioId
//value = prestadorId
//Lo hice así para no meter dependencias directas con entidades de otros módulos dentro del motor.
//O sea:
//solicitudes no necesita conocer la implementación completa de PublicacionServicio
//solo necesita saber qué publicación y qué prestador participan en la distribución
//Eso mantiene mejor el criterio de bajo acoplamiento.


public class MotorDistribucionSolicitudes {

    public List<DistribucionSolicitud> crearDistribucionesIniciales(SolicitudServicio solicitud,
                                                                    Map<UUID, UUID> publicacionesPorPrestador,
                                                                    LocalDateTime fechaEnvio,
                                                                    LocalDateTime fechaExpiracion) {
        List<DistribucionSolicitud> distribuciones = new java.util.ArrayList<>();
        
        if (solicitud == null || publicacionesPorPrestador == null || publicacionesPorPrestador.isEmpty()) {
            return distribuciones;
        }
        
        int ronda = 1;
        Set<UUID> publicacionesIncluidas = new HashSet<>();
        Set<UUID> prestadoresIncluidos = new HashSet<>();
        for (Map.Entry<UUID, UUID> entrada : publicacionesPorPrestador.entrySet()) {
            UUID publicacionId = entrada.getKey();
            UUID prestadorId = entrada.getValue();
            
            if (publicacionId != null && prestadorId != null
                    && publicacionesIncluidas.add(publicacionId)
                    && prestadoresIncluidos.add(prestadorId)) {
                DistribucionSolicitud distribucion = new DistribucionSolicitud(
                        UUID.randomUUID(),
                        solicitud.getId(),
                        publicacionId,
                        prestadorId,
                        EstadoDistribucion.ENVIADA,
                        ronda,
                        fechaEnvio,
                        null,
                        fechaExpiracion
                );
                distribuciones.add(distribucion);
            }
        }
        
        return distribuciones;
    }

    public List<DistribucionSolicitud> crearDistribucionesNuevaRonda(SolicitudServicio solicitud,
                                                                     Map<UUID, UUID> publicacionesPorPrestador,
                                                                     List<DistribucionSolicitud> distribucionesExistentes,
                                                                     LocalDateTime fechaEnvio,
                                                                     LocalDateTime fechaExpiracion) {
        List<DistribucionSolicitud> distribucionesNuevas = new java.util.ArrayList<>();
        
        if (solicitud == null || publicacionesPorPrestador == null || publicacionesPorPrestador.isEmpty()) {
            return distribucionesNuevas;
        }
        
        Integer proximaRonda = calcularSiguienteRonda(distribucionesExistentes);
        
        Set<UUID> publicacionesIncluidas = new HashSet<>();
        Set<UUID> prestadoresIncluidos = new HashSet<>();
        for (Map.Entry<UUID, UUID> entrada : publicacionesPorPrestador.entrySet()) {
            UUID publicacionId = entrada.getKey();
            UUID prestadorId = entrada.getValue();
            
            if (publicacionId != null && prestadorId != null &&
                    !yaFueDistribuidaAPublicacion(publicacionId, distribucionesExistentes) &&
                    !yaFueDistribuidaAPrestador(prestadorId, distribucionesExistentes) &&
                    publicacionesIncluidas.add(publicacionId) &&
                    prestadoresIncluidos.add(prestadorId)) {
                
                DistribucionSolicitud distribucion = new DistribucionSolicitud(
                        UUID.randomUUID(),
                        solicitud.getId(),
                        publicacionId,
                        prestadorId,
                        EstadoDistribucion.ENVIADA,
                        proximaRonda,
                        fechaEnvio,
                        null,
                        fechaExpiracion
                );
                distribucionesNuevas.add(distribucion);
            }
        }
        
        return distribucionesNuevas;
    }

    public boolean debeIniciarDistribucion(SolicitudServicio solicitud) {
        if (solicitud == null) {
            return false;
        }
        
        return solicitud.getEstado() == EstadoSolicitud.BUSCANDO_PRESTADOR;
    }

    public boolean debeAmpliarBusqueda(SolicitudServicio solicitud,
                                       List<DistribucionSolicitud> distribucionesExistentes,
                                       LocalDateTime fechaActual,
                                       Integer tiempoEsperaExpansionMinutos) {
        if (solicitud == null || solicitud.getEstado() != EstadoSolicitud.BUSCANDO_PRESTADOR) {
            return false;
        }
        
        if (distribucionesExistentes == null || distribucionesExistentes.isEmpty()) {
            return true;
        }
        
        if (existenRespuestasFavorables(distribucionesExistentes)) {
            return false;
        }
        
        if (!existenDistribucionesActivas(distribucionesExistentes)) {
            return true;
        }
        
        if (tiempoEsperaExpansionMinutos == null || tiempoEsperaExpansionMinutos <= 0 || fechaActual == null) {
            return false;
        }
        
        LocalDateTime fechaUltimaDistribucion = distribucionesExistentes.stream()
                .map(DistribucionSolicitud::getFechaEnvio)
                .filter(f -> f != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        if (fechaUltimaDistribucion == null) {
            return false;
        }
        
        long minutosTranscurridos = java.time.temporal.ChronoUnit.MINUTES.between(fechaUltimaDistribucion, fechaActual);
        return minutosTranscurridos >= tiempoEsperaExpansionMinutos;
    }

    public boolean existenDistribucionesActivas(List<DistribucionSolicitud> distribuciones) {
        if (distribuciones == null || distribuciones.isEmpty()) {
            return false;
        }
        
        return distribuciones.stream()
                .anyMatch(d -> d != null && d.estaEnviada());
    }

    public boolean existenRespuestasFavorables(List<DistribucionSolicitud> distribuciones) {
        if (distribuciones == null || distribuciones.isEmpty()) {
            return false;
        }
        
        return distribuciones.stream()
                .anyMatch(d -> d != null && (d.estaAceptada() || d.estaContraofertada()));
    }

    public Integer calcularSiguienteRonda(List<DistribucionSolicitud> distribucionesExistentes) {
        if (distribucionesExistentes == null || distribucionesExistentes.isEmpty()) {
            return 1;
        }
        
        Integer maxRonda = distribucionesExistentes.stream()
                .map(DistribucionSolicitud::getRondaDistribucion)
                .filter(r -> r != null)
                .max(Integer::compareTo)
                .orElse(0);
        
        return maxRonda + 1;
    }

    public boolean yaFueDistribuidaAPublicacion(UUID publicacionServicioId,
                                                List<DistribucionSolicitud> distribucionesExistentes) {
        if (publicacionServicioId == null || distribucionesExistentes == null || distribucionesExistentes.isEmpty()) {
            return false;
        }
        
        return distribucionesExistentes.stream()
                .anyMatch(d -> d != null && d.getPublicacionServicioId().equals(publicacionServicioId));
    }

    public boolean yaFueDistribuidaAPrestador(UUID prestadorId,
                                              List<DistribucionSolicitud> distribucionesExistentes) {
        if (prestadorId == null || distribucionesExistentes == null || distribucionesExistentes.isEmpty()) {
            return false;
        }
        
        return distribucionesExistentes.stream()
                .anyMatch(d -> d != null && d.getPrestadorId().equals(prestadorId));
    }

    public boolean puedeReenviarseAlPrestador(UUID prestadorId,
                                              List<DistribucionSolicitud> distribucionesExistentes) {
        if (prestadorId == null || distribucionesExistentes == null) {
            return true;
        }
        
        return distribucionesExistentes.stream()
                .filter(d -> d != null && d.getPrestadorId().equals(prestadorId))
                .noneMatch(DistribucionSolicitud::estaRechazada);
    }

    public boolean debeCerrarCicloDistribucion(SolicitudServicio solicitud,
                                               List<DistribucionSolicitud> distribucionesExistentes) {
        if (solicitud == null) {
            return true;
        }

        EstadoSolicitud estado = solicitud.getEstado();
        if (estado == EstadoSolicitud.CANCELADA || estado == EstadoSolicitud.ASIGNADA || estado == EstadoSolicitud.FINALIZADA) {
            return true;
        }

        if (distribucionesExistentes == null || distribucionesExistentes.isEmpty()) {
            return false;
        }

        // Si ya existe una respuesta favorable (aceptada o contraofertada), cerrar ciclo
        if (existenRespuestasFavorables(distribucionesExistentes)) {
            return true;
        }

        // Si todas las distribuciones están cerradas, rechazadas o expiradas, no queda nada por distribuir
        boolean todasNoDisponibles = distribucionesExistentes.stream()
                .allMatch(d -> d == null || d.estaRechazada() || d.estaExpirada() || d.estaCerrada());

        return todasNoDisponibles;
    }
}
