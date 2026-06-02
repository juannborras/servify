package com.servify.publicaciones.application.service;

import com.servify.publicaciones.application.dto.BuscarPublicacionesCompatiblesQuery;
import com.servify.publicaciones.application.dto.PublicacionCompatibleResult;
import com.servify.publicaciones.application.port.in.BuscarPublicacionesCompatiblesUseCase;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.publicaciones.domain.service.PoliticaCompatibilidadPublicacion;
import com.servify.shared.domain.enumtype.ModalidadServicio;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicacion que coordina la busqueda de publicaciones compatibles.
 */
public class BuscarPublicacionesCompatiblesService implements BuscarPublicacionesCompatiblesUseCase {

    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;
    private final CategoriaServicioRepositoryPort categoriaServicioRepositoryPort;
    private final PoliticaCompatibilidadPublicacion politicaCompatibilidadPublicacion;

    public BuscarPublicacionesCompatiblesService(PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
                                                 CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
                                                 PoliticaCompatibilidadPublicacion politicaCompatibilidadPublicacion) {
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
        this.categoriaServicioRepositoryPort = categoriaServicioRepositoryPort;
        this.politicaCompatibilidadPublicacion = politicaCompatibilidadPublicacion;
    }

    @Override
    public List<PublicacionCompatibleResult> buscarCompatibles(BuscarPublicacionesCompatiblesQuery query) {
        // Valida query, filtra candidatas por compatibilidad y retorna resultados livianos
        if (query == null || query.getCategoriaServicioId() == null) {
            throw new IllegalArgumentException("La query de búsqueda no puede ser nula.");
        }
        CategoriaServicio categoriaRequerida = obtenerCategoriaRequerida(query);
        List<PublicacionServicio> candidatas = obtenerCandidatas(query);
        return candidatas.stream()
                .filter(p -> esCompatible(p, categoriaRequerida, query))
                .filter(p -> estaDentroDelRadio(p, query))
                .map(p -> construirResultado(p, calcularDistanciaKm(p, query)))
                .collect(Collectors.toList());
    }

    // Obtiene la categoría requerida y lanza excepción si no existe
    protected CategoriaServicio obtenerCategoriaRequerida(BuscarPublicacionesCompatiblesQuery query) {
        return categoriaServicioRepositoryPort.buscarPorId(query.getCategoriaServicioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la categoría con id: " + query.getCategoriaServicioId()));
    }

    // Consulta publicaciones activas acotadas por categoría
    protected List<PublicacionServicio> obtenerCandidatas(BuscarPublicacionesCompatiblesQuery query) {
        return publicacionServicioRepositoryPort
                .buscarActivasPorCategoria(query.getCategoriaServicioId());
    }

    // Delega la validación de compatibilidad en PoliticaCompatibilidadPublicacion
    protected boolean esCompatible(PublicacionServicio publicacionServicio,
                                   CategoriaServicio categoriaRequerida,
                                   BuscarPublicacionesCompatiblesQuery query) {
        return politicaCompatibilidadPublicacion.esCompatible(
                publicacionServicio,
                categoriaRequerida,
                query.getModalidadRequerida(),
                query.getUbicacionRequerida(),
                query.getDisponibilidadRequerida()
        );
    }

    // Mapea la publicación al DTO de salida liviano
    protected PublicacionCompatibleResult construirResultado(PublicacionServicio publicacionServicio,
                                                             Double distanciaKm) {
        return PublicacionCompatibleResult.builder()
                .publicacionServicioId(publicacionServicio.getId())
                .prestadorId(publicacionServicio.getUsuarioId())
                .categoriaServicioId(publicacionServicio.getCategoriaServicio().getId())
                .titulo(publicacionServicio.getTitulo())
                .modalidadServicio(publicacionServicio.getModalidadServicio())
                .precioBase(publicacionServicio.getPrecioBase())
                .estado(publicacionServicio.getEstado())
                .distanciaKm(distanciaKm)
                .build();
    }

    // Calcula distancia en km si la modalidad requiere ubicación y hay coordenadas válidas
    private Double calcularDistanciaKm(PublicacionServicio publicacionServicio,
                                       BuscarPublicacionesCompatiblesQuery query) {
        if (ModalidadServicio.VIRTUAL.equals(publicacionServicio.getModalidadServicio())) {
            return null;
        }
        if (query.getUbicacionRequerida() == null
                || !query.getUbicacionRequerida().tieneCoordenadasValidas()) {
            return null;
        }
        return publicacionServicio.getUbicacionesParaMatching().stream()
                .filter(ubicacion -> ubicacion != null && ubicacion.tieneCoordenadasValidas())
                .map(ubicacion -> calcularHaversine(
                        query.getUbicacionRequerida().getLatitud(),
                        query.getUbicacionRequerida().getLongitud(),
                        ubicacion.getLatitud(),
                        ubicacion.getLongitud()
                ))
                .min(Double::compareTo)
                .orElse(null);
    }

    private boolean estaDentroDelRadio(PublicacionServicio publicacionServicio,
                                       BuscarPublicacionesCompatiblesQuery query) {
        if (ModalidadServicio.VIRTUAL.equals(query.getModalidadRequerida())) {
            return true;
        }
        if (query.getRadioBusquedaKm() == null || query.getRadioBusquedaKm() <= 0) {
            return true;
        }
        Double distanciaKm = calcularDistanciaKm(publicacionServicio, query);
        return distanciaKm == null || distanciaKm <= query.getRadioBusquedaKm();
    }

    // Fórmula de Haversine para calcular distancia entre dos coordenadas en km
    private Double calcularHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}
