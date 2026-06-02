package com.servify.shared.infrastructure.web;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public final class MvpWebMapper {

    private MvpWebMapper() {
    }

    public static Ubicacion toUbicacion(UbicacionPayload payload) {
        if (payload == null) {
            return null;
        }
        return new Ubicacion(
                payload.pais,
                payload.provincia,
                payload.ciudad,
                payload.localidad,
                payload.calle,
                payload.altura,
                payload.referencia,
                payload.latitud,
                payload.longitud
        );
    }

    public static DisponibilidadHoraria toDisponibilidad(DisponibilidadPayload payload) {
        if (payload == null) {
            return null;
        }
        return new DisponibilidadHoraria(
                payload.diaSemana,
                payload.horaDesde,
                payload.horaHasta
        );
    }

    public static List<DisponibilidadHoraria> toDisponibilidades(List<DisponibilidadPayload> payloads) {
        if (payloads == null) {
            return Collections.emptyList();
        }
        return payloads.stream()
                .map(MvpWebMapper::toDisponibilidad)
                .toList();
    }

    public static List<Ubicacion> toUbicaciones(List<UbicacionPayload> payloads) {
        if (payloads == null) {
            return Collections.emptyList();
        }
        return payloads.stream()
                .map(MvpWebMapper::toUbicacion)
                .toList();
    }

    public static class UbicacionPayload {
        public String pais;
        public String provincia;
        public String ciudad;
        public String localidad;
        public String calle;
        public String altura;
        public String referencia;
        public Double latitud;
        public Double longitud;
    }

    public static class DisponibilidadPayload {
        public DayOfWeek diaSemana;
        public LocalTime horaDesde;
        public LocalTime horaHasta;
    }
}
