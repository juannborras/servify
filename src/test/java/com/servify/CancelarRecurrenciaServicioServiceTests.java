package com.servify;

import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.CancelarEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.CancelarRecurrenciaServicioCommand;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.CancelarEncuentroServicioService;
import com.servify.solicitudes.application.service.CancelarRecurrenciaServicioService;
import com.servify.solicitudes.application.service.ConfirmarFinalizacionServicioService;
import com.servify.solicitudes.application.service.NotificadorEventosSolicitudService;
import com.servify.solicitudes.domain.enumtype.EstadoAsignacion;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.enumtype.EstadoRecurrenciaServicio;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.CalculadorFechasRecurrencia;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CancelarRecurrenciaServicioServiceTests {

    @Test
    void confirmarAmbasPartesCompletaSoloElEncuentroYGeneraElSiguiente() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        ConfirmacionRepo confirmaciones = new ConfirmacionRepo();
        SolicitudServicio solicitud = solicitud(solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA);
        AsignacionServicio asignacion = asignacion(asignacionId, solicitudId, prestadorId);
        ServicioRecurrencia recurrencia = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        ServicioEncuentro primero = encuentroRecurrente(
                UUID.randomUUID(), solicitudId, asignacionId, recurrencia.getId(), solicitanteId,
                EstadoEncuentroServicio.CONFIRMADO
        );
        solicitudes.guardar(solicitud);
        asignaciones.guardar(asignacion);
        recurrencias.guardar(recurrencia);
        encuentros.guardar(primero);

        ConfirmarFinalizacionServicioService service = new ConfirmarFinalizacionServicioService(
                confirmaciones,
                asignaciones,
                solicitudes,
                new PoliticaFinalizacionMutua(),
                null,
                encuentros,
                recurrencias,
                new CalculadorFechasRecurrencia()
        );

        service.confirmar(new ConfirmarFinalizacionServicioCommand(
                solicitudId, asignacionId, primero.getId(), solicitanteId,
                RolConfirmante.SOLICITANTE, "Encuentro realizado"
        ));
        assertEquals(1, encuentros.buscarPorSolicitudId(solicitudId).size());

        service.confirmar(new ConfirmarFinalizacionServicioCommand(
                solicitudId, asignacionId, primero.getId(), prestadorId,
                RolConfirmante.PRESTADOR, "Encuentro realizado"
        ));

        assertEquals(EstadoEncuentroServicio.COMPLETADO, encuentros.buscarPorId(primero.getId()).orElseThrow().getEstado());
        List<ServicioEncuentro> agenda = encuentros.buscarPorSolicitudId(solicitudId);
        assertEquals(2, agenda.size());
        ServicioEncuentro siguiente = agenda.stream().filter(ServicioEncuentro::estaConfirmado).findFirst().orElseThrow();
        assertEquals(primero.getFechaInicio().toLocalDate().plusWeeks(1), siguiente.getFechaInicio().toLocalDate());
        assertEquals(EstadoSolicitud.ASIGNADA, solicitud.getEstado());
        assertEquals(EstadoAsignacion.ACTIVA, asignacion.getEstado());
        assertEquals(EstadoRecurrenciaServicio.ACTIVA, recurrencia.getEstado());

        // El siguiente encuentro queda visible, pero no se puede cerrar antes de su horario.
        assertThrows(IllegalStateException.class, () -> service.confirmar(
                new ConfirmarFinalizacionServicioCommand(
                        solicitudId, asignacionId, siguiente.getId(), solicitanteId,
                        RolConfirmante.SOLICITANTE, "Segundo encuentro realizado"
                )));
        assertEquals(2, confirmaciones.buscarPorAsignacionServicioId(asignacionId).size());
        assertEquals(3, encuentros.bloqueos);
        assertEquals(3, recurrencias.bloqueos);
    }

    @Test
    void completarUltimoEncuentroFinalizaRecurrenciaYParent() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        ConfirmacionRepo confirmaciones = new ConfirmacionRepo();
        SolicitudServicio solicitud = solicitud(solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA);
        AsignacionServicio asignacion = asignacion(asignacionId, solicitudId, prestadorId);
        ServicioRecurrencia base = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        ServicioEncuentro ultimo = encuentroRecurrente(
                UUID.randomUUID(), solicitudId, asignacionId, base.getId(), solicitanteId,
                EstadoEncuentroServicio.CONFIRMADO
        );
        ServicioRecurrencia recurrencia = new ServicioRecurrencia(
                base.getId(), solicitudId, asignacionId, FrecuenciaRecurrencia.SEMANAL,
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0),
                ultimo.getFechaInicio().toLocalDate(), ultimo.getFechaInicio().toLocalDate(),
                EstadoRecurrenciaServicio.ACTIVA, null, null, null
        );
        solicitudes.guardar(solicitud);
        asignaciones.guardar(asignacion);
        recurrencias.guardar(recurrencia);
        encuentros.guardar(ultimo);
        ConfirmarFinalizacionServicioService service = new ConfirmarFinalizacionServicioService(
                confirmaciones, asignaciones, solicitudes, new PoliticaFinalizacionMutua(),
                null, encuentros, recurrencias, new CalculadorFechasRecurrencia()
        );

        service.confirmar(new ConfirmarFinalizacionServicioCommand(
                solicitudId, asignacionId, ultimo.getId(), solicitanteId,
                RolConfirmante.SOLICITANTE, null
        ));
        service.confirmar(new ConfirmarFinalizacionServicioCommand(
                solicitudId, asignacionId, ultimo.getId(), prestadorId,
                RolConfirmante.PRESTADOR, null
        ));

        assertEquals(EstadoRecurrenciaServicio.FINALIZADA, recurrencia.getEstado());
        assertEquals(EstadoSolicitud.FINALIZADA, solicitud.getEstado());
        assertEquals(EstadoAsignacion.FINALIZADA, asignacion.getEstado());
        assertEquals(1, encuentros.buscarPorSolicitudId(solicitudId).size());
    }

    @Test
    void recurrenciaYaCanceladaReconciliaEstadoHeredadoSinDuplicarNotificacion() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        NotificacionesFake notificaciones = new NotificacionesFake();

        solicitudes.guardar(solicitud(solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA));
        asignaciones.guardar(asignacion(asignacionId, solicitudId, prestadorId));
        ServicioRecurrencia recurrencia = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        recurrencia.cancelar(prestadorId, "Cancelada previamente", LocalDateTime.now().minusHours(1));
        recurrencias.guardar(recurrencia);
        ServicioEncuentro abierto = encuentro(
                UUID.randomUUID(), solicitudId, asignacionId, solicitanteId, EstadoEncuentroServicio.CONFIRMADO
        );
        encuentros.guardar(abierto);

        CancelarRecurrenciaServicioService service = new CancelarRecurrenciaServicioService(
                recurrencias,
                solicitudes,
                asignaciones,
                encuentros,
                new NotificadorEventosSolicitudService(notificaciones)
        );

        ServicioRecurrenciaResult result = service.cancelar(new CancelarRecurrenciaServicioCommand(
                solicitudId,
                prestadorId,
                "Reintento idempotente"
        ));

        assertEquals(EstadoRecurrenciaServicio.CANCELADA, result.getEstado());
        assertEquals(EstadoSolicitud.CANCELADA, solicitudes.buscarPorId(solicitudId).orElseThrow().getEstado());
        assertEquals(EstadoAsignacion.CANCELADA, asignaciones.buscarPorId(asignacionId).orElseThrow().getEstado());
        assertEquals(EstadoEncuentroServicio.CANCELADO, encuentros.buscarPorId(abierto.getId()).orElseThrow().getEstado());
        assertEquals(0, notificaciones.cantidad);
    }

    @Test
    void cancelarUnaVisitaNoCancelaLaSerieNiLaSolicitud() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        NotificacionesFake notificaciones = new NotificacionesFake();

        solicitudes.guardar(solicitud(solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA));
        asignaciones.guardar(asignacion(asignacionId, solicitudId, prestadorId));
        ServicioRecurrencia recurrencia = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        recurrencias.guardar(recurrencia);
        ServicioEncuentro visita = encuentroRecurrente(
                UUID.randomUUID(), solicitudId, asignacionId, recurrencia.getId(), solicitanteId,
                EstadoEncuentroServicio.CONFIRMADO
        );
        encuentros.guardar(visita);

        CancelarEncuentroServicioService service = new CancelarEncuentroServicioService(
                encuentros,
                solicitudes,
                asignaciones,
                new NotificadorEventosSolicitudService(notificaciones),
                recurrencias,
                new CalculadorFechasRecurrencia()
        );

        ServicioEncuentroResult result = service.cancelar(
                new CancelarEncuentroServicioCommand(visita.getId(), prestadorId)
        );

        assertEquals(EstadoEncuentroServicio.CANCELADO, result.getEstado());
        assertEquals(EstadoRecurrenciaServicio.ACTIVA, recurrencia.getEstado());
        assertEquals(EstadoSolicitud.ASIGNADA, solicitudes.buscarPorId(solicitudId).orElseThrow().getEstado());
        assertEquals(EstadoAsignacion.ACTIVA, asignaciones.buscarPorId(asignacionId).orElseThrow().getEstado());
        List<ServicioEncuentro> visitas = encuentros.buscarPorSolicitudId(solicitudId);
        assertEquals(2, visitas.size());
        assertTrue(visitas.stream().anyMatch(actual -> actual.estaConfirmado()
                && actual.getFechaInicio().toLocalDate().equals(visita.getFechaInicio().toLocalDate().plusWeeks(1))));
        assertEquals(1, encuentros.bloqueos);
        assertEquals(1, recurrencias.bloqueos);
        assertEquals(1, notificaciones.cantidad);
        assertEquals(TipoNotificacion.ENCUENTRO_CANCELADO, notificaciones.ultima.getTipo());
    }

    @Test
    void cancelarEncuentroPrincipalProgramadoCancelaSolicitudYAsignacion() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        LocalDateTime inicio = LocalDate.now().plusDays(2).atTime(9, 0);
        LocalDateTime fin = inicio.plusHours(2);

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        SolicitudServicio solicitud = solicitudProgramada(
                solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA, inicio, fin);
        AsignacionServicio asignacion = asignacion(asignacionId, solicitudId, prestadorId);
        ServicioEncuentro principal = new ServicioEncuentro(
                UUID.randomUUID(), solicitudId, asignacionId, solicitanteId,
                inicio, fin, EstadoEncuentroServicio.CONFIRMADO,
                "Fecha programada al crear la solicitud", LocalDateTime.now());
        ServicioEncuentro segunda = new ServicioEncuentro(
                UUID.randomUUID(), solicitudId, asignacionId, solicitanteId,
                inicio.plusWeeks(1), fin.plusWeeks(1), EstadoEncuentroServicio.CONFIRMADO,
                "Segunda visita", LocalDateTime.now());
        solicitudes.guardar(solicitud);
        asignaciones.guardar(asignacion);
        encuentros.guardar(principal);
        encuentros.guardar(segunda);

        CancelarEncuentroServicioService service = new CancelarEncuentroServicioService(
                encuentros, solicitudes, asignaciones, null);

        service.cancelar(new CancelarEncuentroServicioCommand(principal.getId(), prestadorId));

        assertEquals(EstadoEncuentroServicio.CANCELADO, principal.getEstado());
        assertEquals(EstadoEncuentroServicio.CANCELADO, segunda.getEstado());
        assertEquals(EstadoSolicitud.CANCELADA, solicitud.getEstado());
        assertEquals(EstadoAsignacion.CANCELADA, asignacion.getEstado());
    }

    @Test
    void cancelarSegundaVisitaDeProgramadaMantieneSolicitudYAsignacionActivas() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();
        LocalDateTime inicioPrincipal = LocalDate.now().plusDays(2).atTime(9, 0);
        LocalDateTime finPrincipal = inicioPrincipal.plusHours(2);
        LocalDateTime inicioSegunda = inicioPrincipal.plusWeeks(1);

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        SolicitudServicio solicitud = solicitudProgramada(
                solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA, inicioPrincipal, finPrincipal);
        AsignacionServicio asignacion = asignacion(asignacionId, solicitudId, prestadorId);
        ServicioEncuentro segunda = new ServicioEncuentro(
                UUID.randomUUID(), solicitudId, asignacionId, solicitanteId,
                inicioSegunda, inicioSegunda.plusHours(1), EstadoEncuentroServicio.CONFIRMADO,
                "Segunda visita", LocalDateTime.now());
        solicitudes.guardar(solicitud);
        asignaciones.guardar(asignacion);
        encuentros.guardar(segunda);

        CancelarEncuentroServicioService service = new CancelarEncuentroServicioService(
                encuentros, solicitudes, asignaciones, null);

        service.cancelar(new CancelarEncuentroServicioCommand(segunda.getId(), prestadorId));

        assertEquals(EstadoEncuentroServicio.CANCELADO, segunda.getEstado());
        assertEquals(EstadoSolicitud.ASIGNADA, solicitud.getEstado());
        assertEquals(EstadoAsignacion.ACTIVA, asignacion.getEstado());
    }

    @Test
    void prestadorCancelaRecurrenciaCancelaSolicitudYAsignacionYNotificaSolicitante() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        EncuentroRepo encuentros = new EncuentroRepo();
        NotificacionesFake notificaciones = new NotificacionesFake();

        SolicitudServicio solicitud = solicitud(solicitudId, solicitanteId, EstadoSolicitud.ASIGNADA);
        AsignacionServicio asignacion = asignacion(asignacionId, solicitudId, prestadorId);
        ServicioRecurrencia recurrencia = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        solicitudes.guardar(solicitud);
        asignaciones.guardar(asignacion);
        recurrencias.guardar(recurrencia);
        ServicioEncuentro propuesto = encuentro(UUID.randomUUID(), solicitudId, asignacionId, solicitanteId,
                EstadoEncuentroServicio.PROPUESTO);
        ServicioEncuentro confirmado = encuentro(UUID.randomUUID(), solicitudId, asignacionId, prestadorId,
                EstadoEncuentroServicio.CONFIRMADO);
        ServicioEncuentro rechazado = encuentro(UUID.randomUUID(), solicitudId, asignacionId, solicitanteId,
                EstadoEncuentroServicio.RECHAZADO);
        encuentros.guardar(propuesto);
        encuentros.guardar(confirmado);
        encuentros.guardar(rechazado);

        CancelarRecurrenciaServicioService service = new CancelarRecurrenciaServicioService(
                recurrencias,
                solicitudes,
                asignaciones,
                encuentros,
                new NotificadorEventosSolicitudService(notificaciones)
        );

        ServicioRecurrenciaResult result = service.cancelar(new CancelarRecurrenciaServicioCommand(
                solicitudId,
                prestadorId,
                "No puedo continuar"
        ));

        assertEquals(EstadoRecurrenciaServicio.CANCELADA, result.getEstado());
        assertEquals(prestadorId, result.getCanceladaPorId());
        assertEquals(EstadoSolicitud.CANCELADA, solicitudes.buscarPorId(solicitudId).orElseThrow().getEstado());
        assertEquals(EstadoAsignacion.CANCELADA, asignaciones.buscarPorId(asignacionId).orElseThrow().getEstado());
        assertEquals(EstadoEncuentroServicio.CANCELADO, encuentros.buscarPorId(propuesto.getId()).orElseThrow().getEstado());
        assertEquals(EstadoEncuentroServicio.CANCELADO, encuentros.buscarPorId(confirmado.getId()).orElseThrow().getEstado());
        assertEquals(EstadoEncuentroServicio.RECHAZADO, encuentros.buscarPorId(rechazado.getId()).orElseThrow().getEstado());
        assertNotNull(notificaciones.ultima);
        assertEquals(1, notificaciones.cantidad);
        assertEquals(solicitanteId, notificaciones.ultima.getUsuarioId());
        assertEquals(TipoNotificacion.RECURRENCIA_CANCELADA, notificaciones.ultima.getTipo());
        assertEquals(solicitudId, notificaciones.ultima.getReferenciaId());
    }

    @Test
    void noMutaLaRecurrenciaSiLaSolicitudYaEstaCerrada() {
        UUID solicitudId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID asignacionId = UUID.randomUUID();

        SolicitudRepo solicitudes = new SolicitudRepo();
        AsignacionRepo asignaciones = new AsignacionRepo();
        RecurrenciaRepo recurrencias = new RecurrenciaRepo();
        solicitudes.guardar(solicitud(solicitudId, solicitanteId, EstadoSolicitud.FINALIZADA));
        asignaciones.guardar(asignacion(asignacionId, solicitudId, prestadorId));
        ServicioRecurrencia recurrencia = recurrencia(UUID.randomUUID(), solicitudId, asignacionId);
        recurrencias.guardar(recurrencia);

        CancelarRecurrenciaServicioService service = new CancelarRecurrenciaServicioService(
                recurrencias,
                solicitudes,
                asignaciones,
                null
        );

        assertThrows(IllegalStateException.class, () -> service.cancelar(
                new CancelarRecurrenciaServicioCommand(solicitudId, prestadorId, "Fuera de estado")
        ));
        assertEquals(EstadoRecurrenciaServicio.ACTIVA, recurrencia.getEstado());
    }

    private static SolicitudServicio solicitud(UUID id, UUID solicitanteId, EstadoSolicitud estado) {
        return new SolicitudServicio(
                id,
                solicitanteId,
                UUID.randomUUID(),
                ModalidadServicio.PRESENCIAL,
                new Ubicacion("Argentina", "Buenos Aires", "CABA", "Palermo", null, null, null, null, null),
                new DisponibilidadHoraria(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                "Mantenimiento semanal",
                BigDecimal.valueOf(25000),
                estado,
                LocalDateTime.now().minusDays(2),
                TipoProgramacionSolicitud.RECURRENTE,
                null,
                null
        );
    }

    private static SolicitudServicio solicitudProgramada(UUID id,
                                                          UUID solicitanteId,
                                                          EstadoSolicitud estado,
                                                          LocalDateTime inicio,
                                                          LocalDateTime fin) {
        return new SolicitudServicio(
                id,
                solicitanteId,
                UUID.randomUUID(),
                ModalidadServicio.PRESENCIAL,
                new Ubicacion("Argentina", "Buenos Aires", "CABA", "Palermo", null, null, null, null, null),
                new DisponibilidadHoraria(inicio.getDayOfWeek(), inicio.toLocalTime(), fin.toLocalTime()),
                "Servicio programado",
                BigDecimal.valueOf(25000),
                estado,
                LocalDateTime.now().minusDays(2),
                TipoProgramacionSolicitud.PROGRAMADA,
                inicio,
                fin
        );
    }

    private static AsignacionServicio asignacion(UUID id, UUID solicitudId, UUID prestadorId) {
        return new AsignacionServicio(
                id,
                solicitudId,
                UUID.randomUUID(),
                prestadorId,
                UUID.randomUUID(),
                BigDecimal.valueOf(25000),
                EstadoAsignacion.ACTIVA,
                LocalDateTime.now().minusDays(1),
                null
        );
    }

    private static ServicioRecurrencia recurrencia(UUID id, UUID solicitudId, UUID asignacionId) {
        return new ServicioRecurrencia(
                id,
                solicitudId,
                asignacionId,
                FrecuenciaRecurrencia.SEMANAL,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.now(),
                null,
                EstadoRecurrenciaServicio.ACTIVA,
                null,
                null,
                null
        );
    }

    private static ServicioEncuentro encuentro(UUID id,
                                               UUID solicitudId,
                                               UUID asignacionId,
                                               UUID propuestoPorId,
                                               EstadoEncuentroServicio estado) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        return new ServicioEncuentro(
                id,
                solicitudId,
                asignacionId,
                propuestoPorId,
                inicio,
                inicio.plusHours(1),
                estado,
                "Visita programada",
                estado == EstadoEncuentroServicio.PROPUESTO ? null : LocalDateTime.now().minusHours(1)
        );
    }

    private static ServicioEncuentro encuentroRecurrente(UUID id,
                                                          UUID solicitudId,
                                                          UUID asignacionId,
                                                          UUID recurrenciaId,
                                                          UUID propuestoPorId,
                                                          EstadoEncuentroServicio estado) {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        return new ServicioEncuentro(
                id,
                solicitudId,
                asignacionId,
                recurrenciaId,
                propuestoPorId,
                inicio,
                inicio.plusHours(1),
                estado,
                "Visita recurrente programada",
                LocalDateTime.now().minusHours(1)
        );
    }

    private static final class NotificacionesFake implements CrearNotificacionUsuarioUseCase {
        private CrearNotificacionUsuarioCommand ultima;
        private int cantidad;

        @Override
        public NotificacionUsuarioResult crear(CrearNotificacionUsuarioCommand command) {
            ultima = command;
            cantidad++;
            return null;
        }
    }

    private static final class SolicitudRepo implements SolicitudServicioRepositoryPort {
        private final Map<UUID, SolicitudServicio> solicitudes = new LinkedHashMap<>();

        @Override
        public SolicitudServicio guardar(SolicitudServicio solicitudServicio) {
            solicitudes.put(solicitudServicio.getId(), solicitudServicio);
            return solicitudServicio;
        }

        @Override
        public Optional<SolicitudServicio> buscarPorId(UUID solicitudId) {
            return Optional.ofNullable(solicitudes.get(solicitudId));
        }

        @Override
        public List<SolicitudServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return solicitudes.values().stream()
                    .filter(solicitud -> solicitanteId.equals(solicitud.getSolicitanteId()))
                    .toList();
        }
    }

    private static final class AsignacionRepo implements AsignacionServicioRepositoryPort {
        private final Map<UUID, AsignacionServicio> asignaciones = new LinkedHashMap<>();

        @Override
        public AsignacionServicio guardar(AsignacionServicio asignacionServicio) {
            asignaciones.put(asignacionServicio.getId(), asignacionServicio);
            return asignacionServicio;
        }

        @Override
        public Optional<AsignacionServicio> buscarPorId(UUID asignacionServicioId) {
            return Optional.ofNullable(asignaciones.get(asignacionServicioId));
        }

        @Override
        public Optional<AsignacionServicio> buscarPorSolicitudId(UUID solicitudId) {
            return asignaciones.values().stream()
                    .filter(asignacion -> solicitudId.equals(asignacion.getSolicitudId()))
                    .findFirst();
        }

        @Override
        public List<AsignacionServicio> buscarPorPrestadorId(UUID prestadorId) {
            return asignaciones.values().stream()
                    .filter(asignacion -> prestadorId.equals(asignacion.getPrestadorId()))
                    .toList();
        }

        @Override
        public List<AsignacionServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return List.of();
        }
    }

    private static final class RecurrenciaRepo implements ServicioRecurrenciaRepositoryPort {
        private final Map<UUID, ServicioRecurrencia> recurrencias = new LinkedHashMap<>();
        private int bloqueos;

        @Override
        public ServicioRecurrencia guardar(ServicioRecurrencia recurrencia) {
            recurrencias.put(recurrencia.getId(), recurrencia);
            return recurrencia;
        }

        @Override
        public Optional<ServicioRecurrencia> buscarPorId(UUID recurrenciaId) {
            return Optional.ofNullable(recurrencias.get(recurrenciaId));
        }

        @Override
        public Optional<ServicioRecurrencia> buscarPorIdParaActualizar(UUID recurrenciaId) {
            bloqueos++;
            return buscarPorId(recurrenciaId);
        }

        @Override
        public Optional<ServicioRecurrencia> buscarPorSolicitudId(UUID solicitudId) {
            return recurrencias.values().stream()
                    .filter(recurrencia -> solicitudId.equals(recurrencia.getSolicitudId()))
                    .findFirst();
        }
    }

    private static final class EncuentroRepo implements ServicioEncuentroRepositoryPort {
        private final Map<UUID, ServicioEncuentro> encuentros = new LinkedHashMap<>();
        private int bloqueos;

        @Override
        public ServicioEncuentro guardar(ServicioEncuentro encuentro) {
            encuentros.put(encuentro.getId(), encuentro);
            return encuentro;
        }

        @Override
        public Optional<ServicioEncuentro> buscarPorId(UUID encuentroId) {
            return Optional.ofNullable(encuentros.get(encuentroId));
        }

        @Override
        public Optional<ServicioEncuentro> buscarPorIdParaActualizar(UUID encuentroId) {
            bloqueos++;
            return buscarPorId(encuentroId);
        }

        @Override
        public List<ServicioEncuentro> buscarPorSolicitudId(UUID solicitudId) {
            return encuentros.values().stream()
                    .filter(encuentro -> solicitudId.equals(encuentro.getSolicitudId()))
                    .toList();
        }

        @Override
        public List<ServicioEncuentro> buscarPorAsignacionServicioId(UUID asignacionServicioId) {
            return encuentros.values().stream()
                    .filter(encuentro -> asignacionServicioId.equals(encuentro.getAsignacionServicioId()))
                    .toList();
        }
    }

    private static final class ConfirmacionRepo implements ConfirmacionFinalizacionRepositoryPort {
        private final Map<UUID, ConfirmacionFinalizacion> confirmaciones = new LinkedHashMap<>();

        @Override
        public ConfirmacionFinalizacion guardar(ConfirmacionFinalizacion confirmacion) {
            confirmaciones.put(confirmacion.getId(), confirmacion);
            return confirmacion;
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorId(UUID id) {
            return Optional.ofNullable(confirmaciones.get(id));
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorSolicitudId(UUID solicitudId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> solicitudId.equals(confirmacion.getSolicitudId()))
                    .toList();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorAsignacionServicioId(UUID asignacionId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> asignacionId.equals(confirmacion.getAsignacionServicioId()))
                    .toList();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorEncuentroServicioId(UUID encuentroId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> encuentroId.equals(confirmacion.getEncuentroServicioId()))
                    .toList();
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorAsignacionServicioIdYRolConfirmante(
                UUID asignacionId, RolConfirmante rol) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> asignacionId.equals(confirmacion.getAsignacionServicioId()))
                    .filter(confirmacion -> confirmacion.getEncuentroServicioId() == null)
                    .filter(confirmacion -> confirmacion.getRolConfirmante() == rol)
                    .findFirst();
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorEncuentroServicioIdYRolConfirmante(
                UUID encuentroId, RolConfirmante rol) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> encuentroId.equals(confirmacion.getEncuentroServicioId()))
                    .filter(confirmacion -> confirmacion.getRolConfirmante() == rol)
                    .findFirst();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorConfirmanteId(UUID confirmanteId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> confirmanteId.equals(confirmacion.getConfirmanteId()))
                    .toList();
        }
    }
}
