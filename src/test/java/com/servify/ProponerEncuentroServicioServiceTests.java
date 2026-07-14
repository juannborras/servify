package com.servify;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.ProponerEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ResolverEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.ProponerEncuentroServicioService;
import com.servify.solicitudes.application.service.ResolverEncuentroServicioService;
import com.servify.solicitudes.domain.enumtype.EstadoAsignacion;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProponerEncuentroServicioServiceTests {

    @Test
    void rechazaSolicitudesCanceladasOFinalizadasSinPersistir() {
        for (EstadoSolicitud estado : List.of(EstadoSolicitud.CANCELADA, EstadoSolicitud.FINALIZADA)) {
            Fixture fixture = fixture(estado, EstadoAsignacion.ACTIVA);

            assertThrows(IllegalStateException.class, () -> fixture.service().proponer(command(fixture)));
            assertEquals(0, fixture.encuentros().guardados.size());
        }
    }

    @Test
    void rechazaAsignacionesCanceladasOFinalizadasSinPersistir() {
        for (EstadoAsignacion estado : List.of(EstadoAsignacion.CANCELADA, EstadoAsignacion.FINALIZADA)) {
            Fixture fixture = fixture(EstadoSolicitud.ASIGNADA, estado);

            assertThrows(IllegalStateException.class, () -> fixture.service().proponer(command(fixture)));
            assertEquals(0, fixture.encuentros().guardados.size());
        }
    }

    @Test
    void permiteProponerCuandoSolicitudEstaAsignadaYAsignacionActiva() {
        Fixture fixture = fixture(EstadoSolicitud.ASIGNADA, EstadoAsignacion.ACTIVA);

        ServicioEncuentroResult result = fixture.service().proponer(command(fixture));

        assertNotNull(result.getId());
        assertEquals(EstadoEncuentroServicio.PROPUESTO, result.getEstado());
        assertEquals(1, fixture.encuentros().guardados.size());
        assertEquals(fixture.solicitudId(), fixture.encuentros().guardados.getFirst().getSolicitudId());
        assertEquals(fixture.asignacionId(), fixture.encuentros().guardados.getFirst().getAsignacionServicioId());
    }

    @Test
    void resolverEncuentroBloqueaLaFilaAntesDeAceptar() {
        Fixture fixture = fixture(EstadoSolicitud.ASIGNADA, EstadoAsignacion.ACTIVA);
        ServicioEncuentroResult propuesto = fixture.service().proponer(command(fixture));
        ResolverEncuentroServicioService resolver = new ResolverEncuentroServicioService(
                fixture.encuentros(), fixture.solicitudes(), fixture.asignaciones(), null
        );

        ServicioEncuentroResult confirmado = resolver.resolver(new ResolverEncuentroServicioCommand(
                propuesto.getId(), fixture.prestadorId(), TipoDecisionSolicitud.ACEPTAR
        ));

        assertEquals(EstadoEncuentroServicio.CONFIRMADO, confirmado.getEstado());
        assertEquals(1, fixture.encuentros().bloqueos);
    }

    private static Fixture fixture(EstadoSolicitud estadoSolicitud, EstadoAsignacion estadoAsignacion) {
        UUID solicitudId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        SolicitudServicio solicitud = new SolicitudServicio(
                solicitudId,
                solicitanteId,
                UUID.randomUUID(),
                ModalidadServicio.PRESENCIAL,
                new Ubicacion("Argentina", "Buenos Aires", "CABA", "Palermo", null, null, null, null, null),
                new DisponibilidadHoraria(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                "Segunda visita",
                BigDecimal.valueOf(15000),
                estadoSolicitud,
                LocalDateTime.now().minusDays(1)
        );
        AsignacionServicio asignacion = new AsignacionServicio(
                asignacionId,
                solicitudId,
                UUID.randomUUID(),
                prestadorId,
                UUID.randomUUID(),
                BigDecimal.valueOf(15000),
                estadoAsignacion,
                LocalDateTime.now().minusHours(4),
                null
        );
        SolicitudRepo solicitudes = new SolicitudRepo(solicitud);
        AsignacionRepo asignaciones = new AsignacionRepo(asignacion, solicitanteId);
        EncuentroRepo encuentros = new EncuentroRepo();
        ProponerEncuentroServicioService service = new ProponerEncuentroServicioService(
                encuentros,
                solicitudes,
                asignaciones,
                null
        );
        return new Fixture(service, encuentros, solicitudes, asignaciones,
                solicitudId, asignacionId, solicitanteId, prestadorId);
    }

    private static ProponerEncuentroServicioCommand command(Fixture fixture) {
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        return new ProponerEncuentroServicioCommand(
                fixture.solicitudId(),
                fixture.asignacionId(),
                fixture.solicitanteId(),
                inicio,
                inicio.plusHours(1),
                "Revisar el trabajo realizado"
        );
    }

    private record Fixture(ProponerEncuentroServicioService service,
                           EncuentroRepo encuentros,
                           SolicitudRepo solicitudes,
                           AsignacionRepo asignaciones,
                           UUID solicitudId,
                           UUID asignacionId,
                           UUID solicitanteId,
                           UUID prestadorId) {
    }

    private static final class SolicitudRepo implements SolicitudServicioRepositoryPort {
        private SolicitudServicio solicitud;

        private SolicitudRepo(SolicitudServicio solicitud) {
            this.solicitud = solicitud;
        }

        @Override
        public SolicitudServicio guardar(SolicitudServicio solicitudServicio) {
            this.solicitud = solicitudServicio;
            return solicitudServicio;
        }

        @Override
        public Optional<SolicitudServicio> buscarPorId(UUID solicitudId) {
            return solicitud != null && solicitud.getId().equals(solicitudId)
                    ? Optional.of(solicitud)
                    : Optional.empty();
        }

        @Override
        public List<SolicitudServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return solicitud != null && solicitud.getSolicitanteId().equals(solicitanteId)
                    ? List.of(solicitud)
                    : List.of();
        }
    }

    private static final class AsignacionRepo implements AsignacionServicioRepositoryPort {
        private AsignacionServicio asignacion;
        private final UUID solicitanteId;

        private AsignacionRepo(AsignacionServicio asignacion, UUID solicitanteId) {
            this.asignacion = asignacion;
            this.solicitanteId = solicitanteId;
        }

        @Override
        public AsignacionServicio guardar(AsignacionServicio asignacionServicio) {
            this.asignacion = asignacionServicio;
            return asignacionServicio;
        }

        @Override
        public Optional<AsignacionServicio> buscarPorId(UUID asignacionServicioId) {
            return asignacion != null && asignacion.getId().equals(asignacionServicioId)
                    ? Optional.of(asignacion)
                    : Optional.empty();
        }

        @Override
        public Optional<AsignacionServicio> buscarPorSolicitudId(UUID solicitudId) {
            return asignacion != null && asignacion.getSolicitudId().equals(solicitudId)
                    ? Optional.of(asignacion)
                    : Optional.empty();
        }

        @Override
        public List<AsignacionServicio> buscarPorPrestadorId(UUID prestadorId) {
            return asignacion != null && asignacion.getPrestadorId().equals(prestadorId)
                    ? List.of(asignacion)
                    : List.of();
        }

        @Override
        public List<AsignacionServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return this.solicitanteId.equals(solicitanteId) && asignacion != null
                    ? List.of(asignacion)
                    : List.of();
        }
    }

    private static final class EncuentroRepo implements ServicioEncuentroRepositoryPort {
        private final List<ServicioEncuentro> guardados = new ArrayList<>();
        private int bloqueos;

        @Override
        public ServicioEncuentro guardar(ServicioEncuentro encuentro) {
            guardados.add(encuentro);
            return encuentro;
        }

        @Override
        public Optional<ServicioEncuentro> buscarPorId(UUID encuentroId) {
            return guardados.stream().filter(encuentro -> encuentro.getId().equals(encuentroId)).findFirst();
        }

        @Override
        public Optional<ServicioEncuentro> buscarPorIdParaActualizar(UUID encuentroId) {
            bloqueos++;
            return buscarPorId(encuentroId);
        }

        @Override
        public List<ServicioEncuentro> buscarPorSolicitudId(UUID solicitudId) {
            return guardados.stream().filter(encuentro -> encuentro.getSolicitudId().equals(solicitudId)).toList();
        }

        @Override
        public List<ServicioEncuentro> buscarPorAsignacionServicioId(UUID asignacionServicioId) {
            return guardados.stream()
                    .filter(encuentro -> encuentro.getAsignacionServicioId().equals(asignacionServicioId))
                    .toList();
        }
    }
}
