package com.servify.solicitudes.application.service;

import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.CalculadorFechasRecurrencia;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ConfirmarFinalizacionServicioService implements ConfirmarFinalizacionServicioUseCase {

    private final ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final PoliticaFinalizacionMutua politicaFinalizacionMutua;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;
    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;
    private final CalculadorFechasRecurrencia calculadorFechasRecurrencia;
    private final PagoServicioRepositoryPort pagoServicioRepositoryPort;
    private final EstadoIntegracionPagoPort estadoIntegracionPagoPort;

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua
    ) {
        this(confirmacionFinalizacionRepositoryPort, asignacionServicioRepositoryPort,
                solicitudServicioRepositoryPort, politicaFinalizacionMutua, null);
    }

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        this(confirmacionFinalizacionRepositoryPort, asignacionServicioRepositoryPort,
                solicitudServicioRepositoryPort, politicaFinalizacionMutua,
                notificadorEventosSolicitudService, null, null, new CalculadorFechasRecurrencia(), null, null);
    }

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService,
            ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
            ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort,
            CalculadorFechasRecurrencia calculadorFechasRecurrencia
    ) {
        this(confirmacionFinalizacionRepositoryPort, asignacionServicioRepositoryPort,
                solicitudServicioRepositoryPort, politicaFinalizacionMutua, notificadorEventosSolicitudService,
                servicioEncuentroRepositoryPort, servicioRecurrenciaRepositoryPort, calculadorFechasRecurrencia,
                null, null);
    }

    public ConfirmarFinalizacionServicioService(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService,
            ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
            ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort,
            CalculadorFechasRecurrencia calculadorFechasRecurrencia,
            PagoServicioRepositoryPort pagoServicioRepositoryPort,
            EstadoIntegracionPagoPort estadoIntegracionPagoPort
    ) {
        this.confirmacionFinalizacionRepositoryPort = confirmacionFinalizacionRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.politicaFinalizacionMutua = politicaFinalizacionMutua;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
        this.calculadorFechasRecurrencia = calculadorFechasRecurrencia != null
                ? calculadorFechasRecurrencia
                : new CalculadorFechasRecurrencia();
        this.pagoServicioRepositoryPort = pagoServicioRepositoryPort;
        this.estadoIntegracionPagoPort = estadoIntegracionPagoPort;
    }

    @Override
    @Transactional
    public void confirmar(ConfirmarFinalizacionServicioCommand command) {
        validarCommand(command);
        SolicitudServicio solicitud = obtenerSolicitudExistente(command.getSolicitudId());
        AsignacionServicio referenciaAsignacion = asignacionServicioRepositoryPort
                .buscarPorId(command.getAsignacionServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada: " + command.getAsignacionServicioId()));
        validarCorrespondenciaSolicitudAsignacion(solicitud, referenciaAsignacion);

        Optional<ServicioRecurrencia> recurrencia = obtenerRecurrenciaActiva(solicitud);
        Optional<ServicioEncuentro> encuentro = recurrencia.map(actual ->
                obtenerEncuentroRecurrenteActivo(command, referenciaAsignacion, actual));
        // Orden de locks recurrente: recurrencia -> encuentro -> asignacion.
        // Asi coincide con cancelar/completar la serie y evita confirmaciones dobles.
        AsignacionServicio asignacion = obtenerAsignacionExistente(command.getAsignacionServicioId());
        validarCorrespondenciaSolicitudAsignacion(solicitud, asignacion);
        validarAsignacionFinalizable(asignacion);
        validarConfirmante(command, asignacion, solicitud);
        validarMomentoDelEncuentro(encuentro.orElse(null));
        validarPagoAprobado(command, asignacion, encuentro.orElse(null));
        validarAusenciaDeConfirmacionPrevia(command, encuentro.orElse(null));

        LocalDateTime fecha = obtenerFechaActual();
        ConfirmacionFinalizacion confirmacion = construirConfirmacion(command, encuentro.orElse(null), fecha);
        persistirConfirmacion(confirmacion);

        boolean yaFinalizada = asignacion.estaFinalizada();
        List<ConfirmacionFinalizacion> confirmaciones = encuentro
                .map(actual -> confirmacionFinalizacionRepositoryPort.buscarPorEncuentroServicioId(actual.getId()))
                .orElseGet(() -> obtenerConfirmacionesDeAsignacion(asignacion.getId()));

        if (encuentro.isPresent() && recurrencia.isPresent()) {
            evaluarEncuentroRecurrente(solicitud, asignacion, recurrencia.get(), encuentro.get(), confirmaciones, fecha);
        } else {
            evaluarYCerrarSiCorresponde(solicitud, asignacion, confirmaciones, fecha);
        }

        persistirAsignacion(asignacion);
        persistirSolicitud(solicitud);
        notificarFinalizacion(solicitud, asignacion, command.getRolConfirmante(), yaFinalizada);
    }

    private void validarPagoAprobado(ConfirmarFinalizacionServicioCommand command,
                                     AsignacionServicio asignacion,
                                     ServicioEncuentro encuentro) {
        if (estadoIntegracionPagoPort == null || !estadoIntegracionPagoPort.esObligatoria()) return;
        if (pagoServicioRepositoryPort == null) {
            throw new IllegalStateException("La politica de pagos esta habilitada pero no hay repositorio configurado");
        }
        boolean aprobado = pagoServicioRepositoryPort
                .buscarPorObjetivo(asignacion.getId(), encuentro == null ? null : encuentro.getId())
                .map(com.servify.pagos.domain.model.PagoServicio::estaAprobado)
                .orElse(false);
        if (!aprobado) {
            if (!estadoIntegracionPagoPort.estaHabilitada()) {
                throw new IllegalStateException(
                        "Mercado Pago es obligatorio pero no esta configurado; no se puede confirmar el servicio");
            }
            String actor = command.getRolConfirmante() == com.servify.solicitudes.domain.enumtype.RolConfirmante.PRESTADOR
                    ? "El prestador solo puede confirmar el cobro y la finalizacion"
                    : "El solicitante debe pagar con Mercado Pago antes de confirmar";
            throw new IllegalStateException(actor + " cuando el pago figure APROBADO");
        }
    }

    private void validarCommand(ConfirmarFinalizacionServicioCommand command) {
        if (command == null) throw new IllegalArgumentException("El comando no puede ser nulo");
        if (command.getSolicitudId() == null) throw new IllegalArgumentException("solicitudId no puede ser nulo");
        if (command.getAsignacionServicioId() == null) throw new IllegalArgumentException("asignacionServicioId no puede ser nulo");
        if (command.getConfirmanteId() == null) throw new IllegalArgumentException("confirmanteId no puede ser nulo");
        if (command.getRolConfirmante() == null) throw new IllegalArgumentException("rolConfirmante no puede ser nulo");
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected AsignacionServicio obtenerAsignacionExistente(UUID asignacionServicioId) {
        return asignacionServicioRepositoryPort.buscarPorIdParaActualizar(asignacionServicioId)
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada: " + asignacionServicioId));
    }

    protected void validarCorrespondenciaSolicitudAsignacion(SolicitudServicio solicitud,
                                                              AsignacionServicio asignacion) {
        if (solicitud == null || asignacion == null) {
            throw new IllegalArgumentException("Solicitud y asignacion no pueden ser nulas");
        }
        if (!asignacion.correspondeASolicitud(solicitud.getId())) {
            throw new IllegalArgumentException("La asignacion no corresponde a la solicitud indicada");
        }
    }

    protected void validarAsignacionFinalizable(AsignacionServicio asignacion) {
        if (asignacion == null || !asignacion.puedeFinalizarse()) {
            throw new IllegalStateException("La asignacion no esta en estado apto para finalizarse");
        }
    }

    protected void validarConfirmante(ConfirmarFinalizacionServicioCommand command,
                                      AsignacionServicio asignacion,
                                      SolicitudServicio solicitud) {
        switch (command.getRolConfirmante()) {
            case SOLICITANTE -> {
                if (!solicitud.getSolicitanteId().equals(command.getConfirmanteId())) {
                    throw new IllegalArgumentException("El confirmante no es el solicitante de la solicitud");
                }
            }
            case PRESTADOR -> {
                if (!asignacion.getPrestadorId().equals(command.getConfirmanteId())) {
                    throw new IllegalArgumentException("El confirmante no es el prestador de la asignacion");
                }
            }
            default -> throw new IllegalArgumentException("Rol de confirmante no reconocido");
        }
    }

    private Optional<ServicioRecurrencia> obtenerRecurrenciaActiva(SolicitudServicio solicitud) {
        if (!solicitud.esRecurrente()
                || servicioRecurrenciaRepositoryPort == null
                || servicioEncuentroRepositoryPort == null) {
            return Optional.empty();
        }
        ServicioRecurrencia referencia = servicioRecurrenciaRepositoryPort
                .buscarPorSolicitudId(solicitud.getId())
                .orElseThrow(() -> new IllegalStateException("La solicitud recurrente no tiene una recurrencia configurada"));
        ServicioRecurrencia recurrencia = servicioRecurrenciaRepositoryPort
                .buscarPorIdParaActualizar(referencia.getId())
                .orElseThrow(() -> new IllegalStateException("La recurrencia ya no existe"));
        if (!recurrencia.estaActiva()) {
            throw new IllegalStateException("La recurrencia no esta activa");
        }
        return Optional.of(recurrencia);
    }

    private ServicioEncuentro obtenerEncuentroRecurrenteActivo(
            ConfirmarFinalizacionServicioCommand command,
            AsignacionServicio asignacion,
            ServicioRecurrencia recurrencia
    ) {
        if (command.getEncuentroServicioId() != null) {
            ServicioEncuentro encuentro = servicioEncuentroRepositoryPort
                    .buscarPorIdParaActualizar(command.getEncuentroServicioId())
                    .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado: " + command.getEncuentroServicioId()));
            validarEncuentroRecurrente(encuentro, asignacion, recurrencia);
            return encuentro;
        }
        UUID encuentroId = servicioEncuentroRepositoryPort.buscarPorAsignacionServicioId(asignacion.getId()).stream()
                .filter(Objects::nonNull)
                .filter(encuentro -> Objects.equals(encuentro.getRecurrenciaServicioId(), recurrencia.getId()))
                .filter(ServicioEncuentro::estaConfirmado)
                .min(Comparator.comparing(ServicioEncuentro::getFechaInicio))
                .map(ServicioEncuentro::getId)
                .orElseThrow(() -> new IllegalStateException("No hay un encuentro recurrente confirmado para completar"));
        ServicioEncuentro encuentro = servicioEncuentroRepositoryPort.buscarPorIdParaActualizar(encuentroId)
                .orElseThrow(() -> new IllegalStateException("El encuentro recurrente ya no existe"));
        validarEncuentroRecurrente(encuentro, asignacion, recurrencia);
        return encuentro;
    }

    private void validarEncuentroRecurrente(ServicioEncuentro encuentro,
                                            AsignacionServicio asignacion,
                                            ServicioRecurrencia recurrencia) {
        if (!Objects.equals(encuentro.getAsignacionServicioId(), asignacion.getId())
                || !Objects.equals(encuentro.getRecurrenciaServicioId(), recurrencia.getId())) {
            throw new IllegalArgumentException("El encuentro no pertenece a la recurrencia indicada");
        }
        if (!encuentro.estaConfirmado()) {
            throw new IllegalStateException("Solo se puede confirmar la realizacion de un encuentro confirmado");
        }
    }

    private void validarMomentoDelEncuentro(ServicioEncuentro encuentro) {
        if (encuentro != null && encuentro.getFechaFin().isAfter(obtenerFechaActual())) {
            throw new IllegalStateException(
                    "El encuentro recurrente puede pagarse y completarse cuando finaliza su horario programado");
        }
    }

    protected void validarAusenciaDeConfirmacionPrevia(ConfirmarFinalizacionServicioCommand command,
                                                       ServicioEncuentro encuentro) {
        Optional<ConfirmacionFinalizacion> previa = encuentro != null
                ? confirmacionFinalizacionRepositoryPort.buscarPorEncuentroServicioIdYRolConfirmante(
                        encuentro.getId(), command.getRolConfirmante())
                : confirmacionFinalizacionRepositoryPort.buscarPorAsignacionServicioIdYRolConfirmante(
                        command.getAsignacionServicioId(), command.getRolConfirmante());
        if (previa.isPresent() && previa.get().estaConfirmada()) {
            throw new IllegalStateException("Ya existe una confirmacion previa valida para este encuentro y rol");
        }
    }

    protected ConfirmacionFinalizacion construirConfirmacion(ConfirmarFinalizacionServicioCommand command,
                                                             ServicioEncuentro encuentro,
                                                             LocalDateTime fechaConfirmacion) {
        return new ConfirmacionFinalizacion(
                generarIdConfirmacion(),
                command.getSolicitudId(),
                command.getAsignacionServicioId(),
                encuentro != null ? encuentro.getId() : null,
                command.getConfirmanteId(),
                command.getRolConfirmante(),
                true,
                fechaConfirmacion,
                command.getObservacion()
        );
    }

    protected List<ConfirmacionFinalizacion> obtenerConfirmacionesDeAsignacion(UUID asignacionServicioId) {
        return confirmacionFinalizacionRepositoryPort.buscarPorAsignacionServicioId(asignacionServicioId).stream()
                .filter(confirmacion -> confirmacion.getEncuentroServicioId() == null)
                .toList();
    }

    private void evaluarEncuentroRecurrente(SolicitudServicio solicitud,
                                             AsignacionServicio asignacion,
                                             ServicioRecurrencia recurrencia,
                                             ServicioEncuentro encuentro,
                                             List<ConfirmacionFinalizacion> confirmaciones,
                                             LocalDateTime fecha) {
        if (!politicaFinalizacionMutua.puedeFinalizarse(solicitud, confirmaciones)) return;

        encuentro.completar(fecha);
        servicioEncuentroRepositoryPort.guardar(encuentro);

        Optional<ServicioEncuentro> siguiente = crearSiguienteEncuentroSiCorresponde(
                solicitud, asignacion, recurrencia, encuentro);
        if (siguiente.isEmpty()) {
            recurrencia.finalizar();
            servicioRecurrenciaRepositoryPort.guardar(recurrencia);
            asignacion.finalizar(fecha);
            solicitud.marcarComoFinalizada();
        }
    }

    private Optional<ServicioEncuentro> crearSiguienteEncuentroSiCorresponde(
            SolicitudServicio solicitud,
            AsignacionServicio asignacion,
            ServicioRecurrencia recurrencia,
            ServicioEncuentro encuentroActual
    ) {
        Optional<LocalDate> fechaSiguiente = calculadorFechasRecurrencia
                .siguienteFecha(recurrencia, encuentroActual.getFechaInicio().toLocalDate());
        if (fechaSiguiente.isEmpty()) return Optional.empty();

        LocalDateTime inicio = LocalDateTime.of(fechaSiguiente.get(), recurrencia.getHoraDesde());
        Optional<ServicioEncuentro> existente = servicioEncuentroRepositoryPort
                .buscarPorSolicitudId(solicitud.getId()).stream()
                .filter(encuentro -> Objects.equals(encuentro.getRecurrenciaServicioId(), recurrencia.getId()))
                .filter(encuentro -> Objects.equals(encuentro.getFechaInicio(), inicio))
                .findFirst();
        if (existente.isPresent()) return existente;

        ServicioEncuentro siguiente = new ServicioEncuentro(
                UUID.randomUUID(),
                solicitud.getId(),
                asignacion.getId(),
                recurrencia.getId(),
                solicitud.getSolicitanteId(),
                inicio,
                LocalDateTime.of(fechaSiguiente.get(), recurrencia.getHoraHasta()),
                EstadoEncuentroServicio.CONFIRMADO,
                "Siguiente encuentro del servicio recurrente",
                obtenerFechaActual()
        );
        return Optional.of(servicioEncuentroRepositoryPort.guardar(siguiente));
    }

    protected void evaluarYCerrarSiCorresponde(SolicitudServicio solicitud,
                                               AsignacionServicio asignacion,
                                               List<ConfirmacionFinalizacion> confirmaciones,
                                               LocalDateTime fechaFinalizacion) {
        if (politicaFinalizacionMutua.puedeFinalizarse(solicitud, confirmaciones)) {
            asignacion.finalizar(fechaFinalizacion);
            solicitud.marcarComoFinalizada();
        }
    }

    protected void persistirConfirmacion(ConfirmacionFinalizacion confirmacion) {
        confirmacionFinalizacionRepositoryPort.guardar(confirmacion);
    }

    protected void persistirAsignacion(AsignacionServicio asignacion) {
        asignacionServicioRepositoryPort.guardar(asignacion);
    }

    protected void persistirSolicitud(SolicitudServicio solicitud) {
        solicitudServicioRepositoryPort.guardar(solicitud);
    }

    protected UUID generarIdConfirmacion() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    protected void notificarFinalizacion(
            SolicitudServicio solicitud,
            AsignacionServicio asignacion,
            com.servify.solicitudes.domain.enumtype.RolConfirmante rolConfirmante,
            boolean yaFinalizada
    ) {
        if (notificadorEventosSolicitudService == null) return;
        if (!yaFinalizada && asignacion.estaFinalizada()) {
            notificadorEventosSolicitudService.servicioFinalizado(solicitud, asignacion);
            return;
        }
        notificadorEventosSolicitudService.confirmacionFinalizacion(solicitud, asignacion, rolConfirmante);
    }
}
