package com.servify;

import com.servify.pagos.application.dto.IniciarPagoServicioCommand;
import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.dto.SincronizarPagoServicioCommand;
import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.MercadoPagoGatewayPort;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.pagos.application.service.IniciarPagoServicioService;
import com.servify.pagos.application.service.SincronizarPagoServicioService;
import com.servify.pagos.domain.enumtype.EstadoPagoServicio;
import com.servify.pagos.domain.model.PagoServicio;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.ConfirmarFinalizacionServicioService;
import com.servify.solicitudes.domain.enumtype.EstadoAsignacion;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.CalculadorFechasRecurrencia;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PagosMercadoPagoServiceTests {

    @Test
    void bloqueaAmbosRolesSinPagoAprobadoCuandoLaIntegracionEstaHabilitada() {
        Fixture f = new Fixture(false);
        ConfirmarFinalizacionServicioService service = f.finalizacionReal();

        IllegalStateException solicitante = assertThrows(IllegalStateException.class, () -> service.confirmar(
                f.command(RolConfirmante.SOLICITANTE, f.solicitanteId)));
        IllegalStateException prestador = assertThrows(IllegalStateException.class, () -> service.confirmar(
                f.command(RolConfirmante.PRESTADOR, f.prestadorId)));

        assertTrue(solicitante.getMessage().contains("pagar con Mercado Pago"));
        assertTrue(prestador.getMessage().contains("pago figure APROBADO"));
        assertTrue(f.confirmaciones.values.isEmpty());
    }

    @Test
    void politicaObligatoriaNoFallaAbiertoCuandoFaltanCredenciales() {
        Fixture f = new Fixture(false);
        EstadoIntegracionPagoPort obligatoriaSinToken = new EstadoIntegracionPagoPort() {
            @Override public boolean estaHabilitada() { return false; }
            @Override public boolean esObligatoria() { return true; }
        };
        ConfirmarFinalizacionServicioService service = new ConfirmarFinalizacionServicioService(
                f.confirmaciones, f.asignaciones, f.solicitudes, new PoliticaFinalizacionMutua(),
                null, f.encuentros, null, new CalculadorFechasRecurrencia(), f.pagos,
                obligatoriaSinToken);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> service.confirmar(
                f.command(RolConfirmante.SOLICITANTE, f.solicitanteId)));

        assertTrue(error.getMessage().contains("obligatorio pero no esta configurado"));
        assertTrue(f.confirmaciones.values.isEmpty());
    }

    @Test
    void pagoAprobadoRegistraSoloConfirmacionSolicitanteYLaSincronizacionEsIdempotente() {
        Fixture f = new Fixture(false);
        PagoServicio pago = f.pagoPendiente(null);
        f.pagos.guardar(pago);
        f.gateway.pago = f.aprobado(pago);
        CapturingFinalizacion finalizacion = new CapturingFinalizacion(f.confirmaciones);
        SincronizarPagoServicioService service = f.sincronizador(finalizacion);

        PagoServicioResult first = service.sincronizar(new SincronizarPagoServicioCommand(
                pago.getId(), f.solicitanteId, "mp-1"));
        PagoServicioResult second = service.sincronizar(new SincronizarPagoServicioCommand(
                pago.getId(), f.solicitanteId, "mp-1"));

        assertEquals(EstadoPagoServicio.APROBADO, first.estado());
        assertEquals(EstadoPagoServicio.APROBADO, second.estado());
        assertEquals(1, finalizacion.commands.size());
        assertEquals(RolConfirmante.SOLICITANTE, finalizacion.commands.getFirst().getRolConfirmante());
        assertTrue(f.confirmaciones.buscarPorAsignacionServicioIdYRolConfirmante(
                f.asignacionId, RolConfirmante.SOLICITANTE).isPresent());
        assertTrue(f.confirmaciones.buscarPorAsignacionServicioIdYRolConfirmante(
                f.asignacionId, RolConfirmante.PRESTADOR).isEmpty());
    }

    @Test
    void rechazaReferenciaMontoYMonedaQueNoCoinciden() {
        Fixture f = new Fixture(false);

        assertIntegridadRechazada(f, new MercadoPagoGatewayPort.PagoExterno(
                "mp-a", "approved", "otra-referencia", f.precio, "ARS", LocalDateTime.now(), null));
        assertIntegridadRechazada(f, new MercadoPagoGatewayPort.PagoExterno(
                "mp-b", "approved", null, f.precio.add(BigDecimal.ONE), "ARS", LocalDateTime.now(), null));
        assertIntegridadRechazada(f, new MercadoPagoGatewayPort.PagoExterno(
                "mp-c", "approved", null, f.precio, "USD", LocalDateTime.now(), null));
    }

    @Test
    void noPermitePagarElSiguienteEncuentroRecurrenteAntesDeQueFinalice() {
        Fixture f = new Fixture(true);
        ServicioEncuentro segundo = f.encuentro(LocalDateTime.now().plusDays(7));
        f.encuentros.guardar(segundo);
        IniciarPagoServicioService service = f.iniciador();

        PagoServicioResult primero = service.iniciar(new IniciarPagoServicioCommand(
                f.solicitudId, f.solicitanteId, f.asignacionId, f.encuentroId));
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> service.iniciar(
                new IniciarPagoServicioCommand(
                        f.solicitudId, f.solicitanteId, f.asignacionId, segundo.getId())));

        assertEquals(f.encuentroId, primero.encuentroId());
        assertTrue(error.getMessage().contains("finaliza el encuentro"));
        assertEquals(1, f.gateway.preferenciasCreadas);
        assertTrue(f.pagos.buscarPorObjetivo(f.asignacionId, segundo.getId()).isEmpty());
    }

    @Test
    void reutilizaLaMismaPreferenciaParaEvitarDobleCobro() {
        Fixture f = new Fixture(false);
        PagoServicio pago = f.pagoPendiente(null);
        assertEquals(pago.getId(), f.pagoPendiente(null).getId(),
                "el mismo objetivo conserva idempotency key incluso tras rollback");
        pago.registrarPreferencia("pref-existente", "https://mercadopago.test/checkout/pref-existente");
        pago.actualizarEstadoExterno(EstadoPagoServicio.RECHAZADO, "mp-rechazado", "cc_rejected");
        f.pagos.guardar(pago);

        PagoServicioResult result = f.iniciador().iniciar(new IniciarPagoServicioCommand(
                f.solicitudId, f.solicitanteId, f.asignacionId, null));

        assertEquals("https://mercadopago.test/checkout/pref-existente", result.checkoutUrl());
        assertEquals(0, f.gateway.preferenciasCreadas);
    }

    @Test
    void verificacionManualTrasRechazoBuscaElIntentoNuevoPorExternalReference() {
        Fixture f = new Fixture(false);
        PagoServicio pago = f.pagoPendiente(null);
        pago.registrarPreferencia("pref", "https://mercadopago.test/pref");
        pago.actualizarEstadoExterno(EstadoPagoServicio.RECHAZADO, "mp-viejo", "cc_rejected");
        f.pagos.guardar(pago);
        f.gateway.pago = new MercadoPagoGatewayPort.PagoExterno(
                "mp-nuevo", "approved", pago.getExternalReference(), pago.getMonto(), "ARS",
                LocalDateTime.now(), "accredited");

        PagoServicioResult result = f.sincronizador(new CapturingFinalizacion(f.confirmaciones))
                .sincronizar(new SincronizarPagoServicioCommand(pago.getId(), f.solicitanteId, null));

        assertEquals(EstadoPagoServicio.APROBADO, result.estado());
        assertEquals("mp-nuevo", result.mercadoPagoPaymentId());
        assertEquals(0, f.gateway.obtenerCalls);
        assertEquals(1, f.gateway.buscarCalls);
    }

    @Test
    void reembolsoPosteriorActualizaLaVerdadFinanciera() {
        Fixture f = new Fixture(false);
        PagoServicio pago = f.pagoPendiente(null);
        pago.aprobar("mp-1", LocalDateTime.now());
        f.pagos.guardar(pago);
        f.confirmaciones.guardar(f.confirmacionSolicitante(pago));
        f.gateway.pago = new MercadoPagoGatewayPort.PagoExterno(
                "mp-1", "refunded", pago.getExternalReference(), pago.getMonto(), "ARS",
                pago.getAprobadoEn(), "refunded");

        PagoServicioResult result = f.sincronizador(new CapturingFinalizacion(f.confirmaciones))
                .sincronizar(new SincronizarPagoServicioCommand(pago.getId(), f.solicitanteId, "mp-1"));

        assertEquals(EstadoPagoServicio.CANCELADO, result.estado());
        assertFalse(result.canConfirmProvider());
    }

    private static void assertIntegridadRechazada(Fixture f, MercadoPagoGatewayPort.PagoExterno base) {
        PagoServicio pago = f.pagoPendiente(null);
        f.pagos.guardar(pago);
        f.gateway.pago = new MercadoPagoGatewayPort.PagoExterno(
                base.paymentId(), base.estado(), base.externalReference() == null ? pago.getExternalReference() : base.externalReference(),
                base.monto(), base.moneda(), base.fechaAprobacion(), base.detalleEstado());
        assertThrows(IllegalStateException.class, () -> f.sincronizador(new CapturingFinalizacion(f.confirmaciones))
                .sincronizar(new SincronizarPagoServicioCommand(pago.getId(), f.solicitanteId, base.paymentId())));
        f.pagos.values.clear();
    }

    private static final class Fixture {
        final UUID solicitudId = UUID.randomUUID();
        final UUID asignacionId = UUID.randomUUID();
        final UUID solicitanteId = UUID.randomUUID();
        final UUID prestadorId = UUID.randomUUID();
        final UUID encuentroId = UUID.randomUUID();
        final BigDecimal precio = new BigDecimal("12500.00");
        final SolicitudServicio solicitud;
        final AsignacionServicio asignacion;
        final InMemorySolicitudes solicitudes = new InMemorySolicitudes();
        final InMemoryAsignaciones asignaciones = new InMemoryAsignaciones();
        final InMemoryEncuentros encuentros = new InMemoryEncuentros();
        final InMemoryPagos pagos = new InMemoryPagos();
        final InMemoryConfirmaciones confirmaciones = new InMemoryConfirmaciones();
        final FakeGateway gateway = new FakeGateway();
        final EstadoIntegracionPagoPort habilitada = () -> true;

        Fixture(boolean recurrente) {
            solicitud = new SolicitudServicio(solicitudId, solicitanteId, UUID.randomUUID(), null, null, null,
                    "Clases particulares", precio, EstadoSolicitud.ASIGNADA, LocalDateTime.now().minusDays(2),
                    recurrente ? TipoProgramacionSolicitud.RECURRENTE : TipoProgramacionSolicitud.INMEDIATA,
                    null, null);
            asignacion = new AsignacionServicio(asignacionId, solicitudId, UUID.randomUUID(), prestadorId,
                    UUID.randomUUID(), precio, EstadoAsignacion.ACTIVA, LocalDateTime.now().minusDays(1), null);
            solicitudes.guardar(solicitud);
            asignaciones.guardar(asignacion);
            if (recurrente) {
                LocalDateTime inicio = LocalDateTime.now().minusHours(2);
                encuentros.guardar(new ServicioEncuentro(encuentroId, solicitudId, asignacionId,
                        UUID.randomUUID(), solicitanteId, inicio, inicio.plusHours(1),
                        EstadoEncuentroServicio.CONFIRMADO, null, inicio.minusMinutes(5)));
            }
        }

        IniciarPagoServicioService iniciador() {
            return new IniciarPagoServicioService(pagos, gateway, habilitada, solicitudes, asignaciones, encuentros);
        }

        SincronizarPagoServicioService sincronizador(ConfirmarFinalizacionServicioUseCase finalizacion) {
            return new SincronizarPagoServicioService(pagos, gateway, habilitada, finalizacion, confirmaciones);
        }

        ConfirmarFinalizacionServicioService finalizacionReal() {
            return new ConfirmarFinalizacionServicioService(confirmaciones, asignaciones, solicitudes,
                    new PoliticaFinalizacionMutua(), null, encuentros, null,
                    new CalculadorFechasRecurrencia(), pagos, habilitada);
        }

        ConfirmarFinalizacionServicioCommand command(RolConfirmante rol, UUID actor) {
            return new ConfirmarFinalizacionServicioCommand(solicitudId, asignacionId, null, actor, rol, null);
        }

        PagoServicio pagoPendiente(UUID encuentroId) {
            PagoServicio pago = PagoServicio.nuevo(solicitudId, asignacionId, encuentroId, solicitanteId, precio);
            pago.marcarCreacion(LocalDateTime.now());
            return pago;
        }

        MercadoPagoGatewayPort.PagoExterno aprobado(PagoServicio pago) {
            return new MercadoPagoGatewayPort.PagoExterno("mp-1", "approved", pago.getExternalReference(),
                    pago.getMonto(), "ARS", LocalDateTime.now(), "accredited");
        }

        ServicioEncuentro encuentro(LocalDateTime inicio) {
            return new ServicioEncuentro(UUID.randomUUID(), solicitudId, asignacionId, UUID.randomUUID(),
                    solicitanteId, inicio, inicio.plusHours(1), EstadoEncuentroServicio.CONFIRMADO, null, inicio);
        }

        ConfirmacionFinalizacion confirmacionSolicitante(PagoServicio pago) {
            return new ConfirmacionFinalizacion(UUID.randomUUID(), solicitudId, asignacionId,
                    pago.getEncuentroServicioId(), solicitanteId, RolConfirmante.SOLICITANTE,
                    true, LocalDateTime.now(), "pago");
        }
    }

    private static final class FakeGateway implements MercadoPagoGatewayPort {
        PagoExterno pago;
        int preferenciasCreadas;
        int obtenerCalls;
        int buscarCalls;
        @Override public PreferenciaCreada crearPreferencia(SolicitudPreferencia solicitud) {
            preferenciasCreadas++;
            return new PreferenciaCreada("pref-" + solicitud.pagoId(), "https://mp.test/" + solicitud.pagoId());
        }
        @Override public PagoExterno obtenerPago(String paymentId) { obtenerCalls++; return pago; }
        @Override public Optional<PagoExterno> buscarPagoPorExternalReference(String externalReference) {
            buscarCalls++;
            return Optional.ofNullable(pago);
        }
    }

    private static final class CapturingFinalizacion implements ConfirmarFinalizacionServicioUseCase {
        final List<ConfirmarFinalizacionServicioCommand> commands = new ArrayList<>();
        final InMemoryConfirmaciones repository;
        CapturingFinalizacion(InMemoryConfirmaciones repository) { this.repository = repository; }
        @Override public void confirmar(ConfirmarFinalizacionServicioCommand command) {
            commands.add(command);
            repository.guardar(new ConfirmacionFinalizacion(UUID.randomUUID(), command.getSolicitudId(),
                    command.getAsignacionServicioId(), command.getEncuentroServicioId(), command.getConfirmanteId(),
                    command.getRolConfirmante(), true, LocalDateTime.now(), command.getObservacion()));
        }
    }

    private static final class InMemoryPagos implements PagoServicioRepositoryPort {
        final Map<UUID, PagoServicio> values = new HashMap<>();
        @Override public PagoServicio guardar(PagoServicio pago) { values.put(pago.getId(), pago); return pago; }
        @Override public Optional<PagoServicio> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public Optional<PagoServicio> buscarPorIdParaActualizar(UUID id) { return buscarPorId(id); }
        @Override public Optional<PagoServicio> buscarPorObjetivo(UUID asignacionId, UUID encuentroId) {
            return values.values().stream().filter(p -> p.getAsignacionServicioId().equals(asignacionId)
                    && java.util.Objects.equals(p.getEncuentroServicioId(), encuentroId)).findFirst();
        }
        @Override public Optional<PagoServicio> buscarPorObjetivoParaActualizar(UUID a, UUID e) { return buscarPorObjetivo(a, e); }
        @Override public Optional<PagoServicio> buscarPorExternalReference(String value) {
            return values.values().stream().filter(p -> p.getExternalReference().equals(value)).findFirst();
        }
        @Override public Optional<PagoServicio> buscarPorExternalReferenceParaActualizar(String value) { return buscarPorExternalReference(value); }
        @Override public Optional<PagoServicio> buscarPorMercadoPagoPaymentId(String value) {
            return values.values().stream().filter(p -> value.equals(p.getMercadoPagoPaymentId())).findFirst();
        }
    }

    private static final class InMemorySolicitudes implements SolicitudServicioRepositoryPort {
        final Map<UUID, SolicitudServicio> values = new HashMap<>();
        @Override public SolicitudServicio guardar(SolicitudServicio value) { values.put(value.getId(), value); return value; }
        @Override public Optional<SolicitudServicio> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<SolicitudServicio> buscarPorSolicitanteId(UUID id) { return values.values().stream().filter(s -> s.getSolicitanteId().equals(id)).toList(); }
    }

    private static final class InMemoryAsignaciones implements AsignacionServicioRepositoryPort {
        final Map<UUID, AsignacionServicio> values = new HashMap<>();
        @Override public AsignacionServicio guardar(AsignacionServicio value) { values.put(value.getId(), value); return value; }
        @Override public Optional<AsignacionServicio> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public Optional<AsignacionServicio> buscarPorSolicitudId(UUID id) { return values.values().stream().filter(a -> a.getSolicitudId().equals(id)).findFirst(); }
        @Override public List<AsignacionServicio> buscarPorPrestadorId(UUID id) { return values.values().stream().filter(a -> a.getPrestadorId().equals(id)).toList(); }
        @Override public List<AsignacionServicio> buscarPorSolicitanteId(UUID id) { return List.of(); }
    }

    private static final class InMemoryEncuentros implements ServicioEncuentroRepositoryPort {
        final Map<UUID, ServicioEncuentro> values = new HashMap<>();
        @Override public ServicioEncuentro guardar(ServicioEncuentro value) { values.put(value.getId(), value); return value; }
        @Override public Optional<ServicioEncuentro> buscarPorId(UUID id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<ServicioEncuentro> buscarPorSolicitudId(UUID id) { return values.values().stream().filter(e -> e.getSolicitudId().equals(id)).toList(); }
        @Override public List<ServicioEncuentro> buscarPorAsignacionServicioId(UUID id) { return values.values().stream().filter(e -> e.getAsignacionServicioId().equals(id)).toList(); }
    }

    private static final class InMemoryConfirmaciones implements ConfirmacionFinalizacionRepositoryPort {
        final List<ConfirmacionFinalizacion> values = new ArrayList<>();
        @Override public ConfirmacionFinalizacion guardar(ConfirmacionFinalizacion value) { values.add(value); return value; }
        @Override public Optional<ConfirmacionFinalizacion> buscarPorId(UUID id) { return values.stream().filter(c -> c.getId().equals(id)).findFirst(); }
        @Override public List<ConfirmacionFinalizacion> buscarPorSolicitudId(UUID id) { return values.stream().filter(c -> c.getSolicitudId().equals(id)).toList(); }
        @Override public List<ConfirmacionFinalizacion> buscarPorAsignacionServicioId(UUID id) { return values.stream().filter(c -> c.getAsignacionServicioId().equals(id)).toList(); }
        @Override public List<ConfirmacionFinalizacion> buscarPorEncuentroServicioId(UUID id) { return values.stream().filter(c -> id.equals(c.getEncuentroServicioId())).toList(); }
        @Override public Optional<ConfirmacionFinalizacion> buscarPorAsignacionServicioIdYRolConfirmante(UUID id, RolConfirmante rol) {
            return values.stream().filter(c -> c.getAsignacionServicioId().equals(id) && c.getEncuentroServicioId() == null && c.getRolConfirmante() == rol).findFirst();
        }
        @Override public Optional<ConfirmacionFinalizacion> buscarPorEncuentroServicioIdYRolConfirmante(UUID id, RolConfirmante rol) {
            return values.stream().filter(c -> id.equals(c.getEncuentroServicioId()) && c.getRolConfirmante() == rol).findFirst();
        }
        @Override public List<ConfirmacionFinalizacion> buscarPorConfirmanteId(UUID id) { return values.stream().filter(c -> c.getConfirmanteId().equals(id)).toList(); }
    }
}
