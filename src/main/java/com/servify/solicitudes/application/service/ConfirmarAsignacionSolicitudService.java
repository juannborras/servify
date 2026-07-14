package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.dto.ConfirmarAsignacionSolicitudCommand;
import com.servify.solicitudes.application.port.in.ConfirmarAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.PoliticaAsignacionUnica;
import com.servify.solicitudes.domain.service.CalculadorFechasRecurrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ConfirmarAsignacionSolicitudService implements ConfirmarAsignacionSolicitudUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final PoliticaAsignacionUnica politicaAsignacionUnica;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;
    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;
    private final CalculadorFechasRecurrencia calculadorFechasRecurrencia = new CalculadorFechasRecurrencia();

    public ConfirmarAsignacionSolicitudService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                               DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                               AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                               PoliticaAsignacionUnica politicaAsignacionUnica) {
        this(
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort,
                politicaAsignacionUnica,
                null,
                null,
                null
        );
    }

    public ConfirmarAsignacionSolicitudService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                               DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                               AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                               PoliticaAsignacionUnica politicaAsignacionUnica,
                                               NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this(
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort,
                politicaAsignacionUnica,
                notificadorEventosSolicitudService,
                null,
                null
        );
    }

    public ConfirmarAsignacionSolicitudService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                               DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                               AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                               PoliticaAsignacionUnica politicaAsignacionUnica,
                                               NotificadorEventosSolicitudService notificadorEventosSolicitudService,
                                               ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                               ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.politicaAsignacionUnica = politicaAsignacionUnica;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
    }

    @Override
    public AsignacionServicioResult confirmar(ConfirmarAsignacionSolicitudCommand command) {
        // Confirma la asignación de una solicitud basada en una distribución enviada.
        // - valida la pertenencia del solicitante
        // - crea la entidad AsignacionServicio y cierra distribuciones si procede
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitudId() == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        if (command.getDistribucionSolicitudId() == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }

        SolicitudServicio solicitud = obtenerSolicitudExistente(command.getSolicitudId());
        DistribucionSolicitud distribucion = obtenerDistribucionExistente(command.getDistribucionSolicitudId());

        validarSolicitudDelSolicitante(solicitud, command.getSolicitanteId());
        validarDistribucionConfirmable(solicitud, distribucion);

        // validar politica de asignacion unica
        java.util.List<AsignacionServicio> asignacionesExistentes = new java.util.ArrayList<>();
        this.asignacionServicioRepositoryPort.buscarPorSolicitudId(solicitud.getId()).ifPresent(asignacionesExistentes::add);
        if (!this.politicaAsignacionUnica.puedeAsignarse(solicitud, asignacionesExistentes)) {
            throw new IllegalStateException("No se permite asignación única para esta solicitud");
        }

        LocalDateTime ahora = obtenerFechaActual();
        AsignacionServicio asignacion = construirAsignacion(solicitud, distribucion, ahora);
        AsignacionServicio asignacionGuardada = this.asignacionServicioRepositoryPort.guardar(asignacion);

        solicitud.marcarComoAsignada();
        this.solicitudServicioRepositoryPort.guardar(solicitud);

        if (this.politicaAsignacionUnica.requiereCerrarDistribucionesRestantes()) {
            cerrarDistribucionesRestantes(solicitud.getId(), distribucion.getId());
        }
        notificarAsignacion(solicitud, asignacionGuardada);
        registrarAgendaInicial(solicitud, asignacionGuardada);

        return construirResultado(asignacionGuardada);
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected DistribucionSolicitud obtenerDistribucionExistente(UUID distribucionSolicitudId) {
        if (distribucionSolicitudId == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        return this.distribucionSolicitudRepositoryPort.buscarPorId(distribucionSolicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Distribución no encontrada: " + distribucionSolicitudId));
    }

    protected void validarSolicitudDelSolicitante(SolicitudServicio solicitudServicio,
                                                  UUID solicitanteId) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("Solicitud no puede ser nula");
        }
        if (solicitanteId == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        if (!solicitudServicio.getSolicitanteId().equals(solicitanteId)) {
            throw new IllegalArgumentException("El solicitante no es propietario de la solicitud");
        }
    }

    protected void validarDistribucionConfirmable(SolicitudServicio solicitudServicio,
                                                  DistribucionSolicitud distribucionSolicitud) {
        if (solicitudServicio == null || distribucionSolicitud == null) {
            throw new IllegalArgumentException("Solicitud o distribución no puede ser nula");
        }
        if (!distribucionSolicitud.getSolicitudId().equals(solicitudServicio.getId())) {
            throw new IllegalStateException("La distribución no corresponde a la solicitud indicada");
        }
        if (!(distribucionSolicitud.estaAceptada() || distribucionSolicitud.estaContraofertada())) {
            throw new IllegalStateException("La distribución no se encuentra en un estado confirmable");
        }
    }

    protected AsignacionServicio construirAsignacion(SolicitudServicio solicitudServicio,
                                                     DistribucionSolicitud distribucionSolicitud,
                                                     LocalDateTime fechaAsignacion) {
        java.util.UUID id = generarIdAsignacion();
        java.math.BigDecimal precio = solicitudServicio.getPrecioReferencia();
        return new AsignacionServicio(
            id,
            solicitudServicio.getId(),
            distribucionSolicitud.getId(),
            distribucionSolicitud.getPrestadorId(),
            distribucionSolicitud.getPublicacionServicioId(),
            precio,
            com.servify.solicitudes.domain.enumtype.EstadoAsignacion.ACTIVA,
            fechaAsignacion,
            null
        );
    }

    protected void cerrarDistribucionesRestantes(UUID solicitudId,
                                                 UUID distribucionConfirmadaId) {
        if (solicitudId == null) {
            return;
        }
        java.util.List<DistribucionSolicitud> activas = this.distribucionSolicitudRepositoryPort.buscarActivasPorSolicitudId(solicitudId);
        if (activas == null || activas.isEmpty()) {
            return;
        }
        for (DistribucionSolicitud d : activas) {
            if (d == null) continue;
            if (distribucionConfirmadaId != null && d.getId().equals(distribucionConfirmadaId)) continue;
            d.cerrar();
            this.distribucionSolicitudRepositoryPort.guardar(d);
        }
    }

    protected AsignacionServicioResult construirResultado(AsignacionServicio asignacionServicio) {
        if (asignacionServicio == null) {
            return null;
        }
        return AsignacionServicioResult.builder()
                .id(asignacionServicio.getId())
                .solicitudId(asignacionServicio.getSolicitudId())
                .distribucionSolicitudId(asignacionServicio.getDistribucionSolicitudId())
                .prestadorId(asignacionServicio.getPrestadorId())
                .publicacionServicioId(asignacionServicio.getPublicacionServicioId())
                .precioAcordado(asignacionServicio.getPrecioAcordado())
                .estado(asignacionServicio.getEstado())
                .fechaAsignacion(asignacionServicio.getFechaAsignacion())
                .fechaFinalizacion(asignacionServicio.getFechaFinalizacion())
                .build();
    }

    protected UUID generarIdAsignacion() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    protected void notificarAsignacion(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.servicioAsignado(solicitud, asignacion);
        }
    }

    private void registrarAgendaInicial(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        if (solicitud == null || asignacion == null || servicioEncuentroRepositoryPort == null) {
            return;
        }
        if (solicitud.esProgramada()
                && solicitud.getFechaProgramadaInicio() != null
                && solicitud.getFechaProgramadaFin() != null) {
            guardarEncuentroConfirmado(
                    solicitud,
                    asignacion,
                    null,
                    solicitud.getFechaProgramadaInicio(),
                    solicitud.getFechaProgramadaFin(),
                    "Fecha programada al crear la solicitud"
            );
        }
        if (solicitud.esRecurrente() && servicioRecurrenciaRepositoryPort != null) {
            servicioRecurrenciaRepositoryPort.buscarPorSolicitudId(solicitud.getId()).ifPresent(recurrencia -> {
                recurrencia.activar(asignacion.getId());
                ServicioRecurrencia guardada = servicioRecurrenciaRepositoryPort.guardar(recurrencia);
                crearPrimerEncuentroRecurrente(solicitud, asignacion, guardada);
            });
        }
    }

    private void crearPrimerEncuentroRecurrente(SolicitudServicio solicitud,
                                                AsignacionServicio asignacion,
                                                ServicioRecurrencia recurrencia) {
        if (recurrencia.getFechaInicio() == null || recurrencia.getDiaSemana() == null) {
            return;
        }
        LocalDate fecha = calculadorFechasRecurrencia.primeraFecha(recurrencia).orElse(null);
        if (fecha == null) return;
        guardarEncuentroConfirmado(
                solicitud,
                asignacion,
                recurrencia.getId(),
                LocalDateTime.of(fecha, recurrencia.getHoraDesde()),
                LocalDateTime.of(fecha, recurrencia.getHoraHasta()),
                "Primer encuentro de servicio recurrente"
        );
    }

    private void guardarEncuentroConfirmado(SolicitudServicio solicitud,
                                            AsignacionServicio asignacion,
                                            UUID recurrenciaServicioId,
                                            LocalDateTime fechaInicio,
                                            LocalDateTime fechaFin,
                                            String mensaje) {
        ServicioEncuentro encuentro = new ServicioEncuentro(
                UUID.randomUUID(),
                solicitud.getId(),
                asignacion.getId(),
                recurrenciaServicioId,
                solicitud.getSolicitanteId(),
                fechaInicio,
                fechaFin,
                EstadoEncuentroServicio.CONFIRMADO,
                mensaje,
                obtenerFechaActual()
        );
        servicioEncuentroRepositoryPort.guardar(encuentro);
    }
}
