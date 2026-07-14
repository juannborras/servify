package com.servify;

import com.servify.solicitudes.application.dto.AcordarPrecioAsignacionCommand;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.AcordarPrecioAsignacionService;
import com.servify.solicitudes.domain.enumtype.EstadoAsignacion;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcordarPrecioAsignacionServiceTests {

    @Test
    void solicitanteRegistraPrecioYQuedaDisponibleEnAsignacion() {
        Fixture fixture = new Fixture(null);

        AsignacionServicioResult result = fixture.service.acordar(new AcordarPrecioAsignacionCommand(
                fixture.solicitudId,
                fixture.asignacionId,
                fixture.solicitanteId,
                new BigDecimal("20000.00")
        ));

        assertEquals(new BigDecimal("20000.00"), result.getPrecioAcordado());
        assertEquals(new BigDecimal("20000.00"), fixture.asignacion.getPrecioAcordado());
    }

    @Test
    void otroUsuarioNoPuedeRegistrarElPrecio() {
        Fixture fixture = new Fixture(null);

        assertThrows(IllegalArgumentException.class, () -> fixture.service.acordar(
                new AcordarPrecioAsignacionCommand(
                        fixture.solicitudId,
                        fixture.asignacionId,
                        UUID.randomUUID(),
                        new BigDecimal("20000.00")
                )
        ));
    }

    @Test
    void noPermiteReemplazarUnPrecioYaAcordado() {
        Fixture fixture = new Fixture(new BigDecimal("15000.00"));

        assertThrows(IllegalStateException.class, () -> fixture.service.acordar(
                new AcordarPrecioAsignacionCommand(
                        fixture.solicitudId,
                        fixture.asignacionId,
                        fixture.solicitanteId,
                        new BigDecimal("18000.00")
                )
        ));
    }

    private static final class Fixture {
        final UUID solicitudId = UUID.randomUUID();
        final UUID asignacionId = UUID.randomUUID();
        final UUID solicitanteId = UUID.randomUUID();
        final SolicitudServicio solicitud;
        final AsignacionServicio asignacion;
        final SolicitudesRepo solicitudes = new SolicitudesRepo();
        final AsignacionesRepo asignaciones = new AsignacionesRepo();
        final AcordarPrecioAsignacionService service;

        Fixture(BigDecimal precioInicial) {
            solicitud = new SolicitudServicio(
                    solicitudId,
                    solicitanteId,
                    UUID.randomUUID(),
                    null,
                    null,
                    null,
                    "Servicio a convenir",
                    null,
                    EstadoSolicitud.ASIGNADA,
                    LocalDateTime.now().minusDays(1),
                    TipoProgramacionSolicitud.INMEDIATA,
                    null,
                    null
            );
            asignacion = new AsignacionServicio(
                    asignacionId,
                    solicitudId,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    precioInicial,
                    EstadoAsignacion.ACTIVA,
                    LocalDateTime.now(),
                    null
            );
            solicitudes.guardar(solicitud);
            asignaciones.guardar(asignacion);
            service = new AcordarPrecioAsignacionService(solicitudes, asignaciones);
        }
    }

    private static final class SolicitudesRepo implements SolicitudServicioRepositoryPort {
        private final Map<UUID, SolicitudServicio> values = new HashMap<>();
        @Override public SolicitudServicio guardar(SolicitudServicio value) { values.put(value.getId(), value); return value; }
        @Override public Optional<SolicitudServicio> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<SolicitudServicio> buscarPorSolicitanteId(UUID id) {
            return values.values().stream().filter(value -> value.getSolicitanteId().equals(id)).toList();
        }
    }

    private static final class AsignacionesRepo implements AsignacionServicioRepositoryPort {
        private final Map<UUID, AsignacionServicio> values = new HashMap<>();
        @Override public AsignacionServicio guardar(AsignacionServicio value) { values.put(value.getId(), value); return value; }
        @Override public Optional<AsignacionServicio> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public Optional<AsignacionServicio> buscarPorSolicitudId(UUID id) {
            return values.values().stream().filter(value -> value.getSolicitudId().equals(id)).findFirst();
        }
        @Override public List<AsignacionServicio> buscarPorPrestadorId(UUID id) {
            return values.values().stream().filter(value -> value.getPrestadorId().equals(id)).toList();
        }
        @Override public List<AsignacionServicio> buscarPorSolicitanteId(UUID id) { return List.of(); }
    }
}
