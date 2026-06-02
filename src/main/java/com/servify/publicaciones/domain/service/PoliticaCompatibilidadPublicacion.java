package com.servify.publicaciones.domain.service;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

public class PoliticaCompatibilidadPublicacion {

    // Validación integral: publicación apta, categoría, modalidad, ubicación y disponibilidad compatibles
    public boolean esCompatible(PublicacionServicio publicacion,
                                CategoriaServicio categoriaRequerida,
                                ModalidadServicio modalidadRequerida,
                                Ubicacion ubicacionRequerida,
                                DisponibilidadHoraria disponibilidadRequerida) {
        if (publicacion == null) {
            return false;
        }
        if (!publicacion.puedeParticiparEnDistribucion()) {
            return false;
        }
        if (!esCompatiblePorCategoria(publicacion, categoriaRequerida)) {
            return false;
        }
        if (!esCompatiblePorModalidad(publicacion, modalidadRequerida)) {
            return false;
        }
        if (!ModalidadServicio.VIRTUAL.equals(modalidadRequerida)
                && !esCompatiblePorUbicacion(publicacion, ubicacionRequerida)) {
            return false;
        }
        if (!esCompatiblePorDisponibilidad(publicacion, disponibilidadRequerida)) {
            return false;
        }
        return true;
    }

    // Verifica si la categoría de la publicación coincide con la requerida
    public boolean esCompatiblePorCategoria(PublicacionServicio publicacion,
                                            CategoriaServicio categoriaRequerida) {
        if (publicacion == null || categoriaRequerida == null) {
            return false;
        }
        return publicacion.getCategoriaServicio() != null
                && publicacion.getCategoriaServicio().mismoIdQue(categoriaRequerida);
    }

    // Verifica si la modalidad satisface la requerida (MIXTA cubre PRESENCIAL y VIRTUAL)
    public boolean esCompatiblePorModalidad(PublicacionServicio publicacion,
                                            ModalidadServicio modalidadRequerida) {
        if (publicacion == null || modalidadRequerida == null) {
            return false;
        }
        ModalidadServicio modalidadPublicacion = publicacion.getModalidadServicio();
        if (modalidadPublicacion == null) {
            return false;
        }
        if (modalidadPublicacion.equals(modalidadRequerida)) {
            return true;
        }
        if (ModalidadServicio.MIXTA.equals(modalidadPublicacion)) {
            return ModalidadServicio.PRESENCIAL.equals(modalidadRequerida)
                    || ModalidadServicio.VIRTUAL.equals(modalidadRequerida);
        }
        return false;
    }

    // Verifica compatibilidad geográfica por ciudad y provincia, o por coordenadas
    public boolean esCompatiblePorUbicacion(PublicacionServicio publicacion,
                                            Ubicacion ubicacionRequerida) {
        if (publicacion == null || ubicacionRequerida == null) {
            return false;
        }
        // Para modalidad VIRTUAL la ubicación no es relevante
        if (ModalidadServicio.VIRTUAL.equals(publicacion.getModalidadServicio())) {
            return true;
        }
        return publicacion.getUbicacionesParaMatching().stream()
                .anyMatch(ubicacionPublicacion -> {
                    boolean mismaCiudad = ubicacionPublicacion.getCiudad() != null
                            && ubicacionPublicacion.getCiudad().equalsIgnoreCase(ubicacionRequerida.getCiudad());
                    boolean mismaProvincia = ubicacionPublicacion.getProvincia() != null
                            && ubicacionPublicacion.getProvincia().equalsIgnoreCase(ubicacionRequerida.getProvincia());
                    return mismaCiudad && mismaProvincia;
                });
    }

    // Verifica si alguna disponibilidad de la publicación es compatible con la requerida
    public boolean esCompatiblePorDisponibilidad(PublicacionServicio publicacion,
                                                 DisponibilidadHoraria disponibilidadRequerida) {
        if (publicacion == null || disponibilidadRequerida == null) {
            return false;
        }
        if (publicacion.getDisponibilidadesHorarias() == null
                || publicacion.getDisponibilidadesHorarias().isEmpty()) {
            return false;
        }
        return publicacion.getDisponibilidadesHorarias().stream()
                .anyMatch(d -> d.getDiaSemana().equals(disponibilidadRequerida.getDiaSemana())
                        && !d.getHoraDesde().isAfter(disponibilidadRequerida.getHoraDesde())
                        && !d.getHoraHasta().isBefore(disponibilidadRequerida.getHoraHasta()));
    }
}
