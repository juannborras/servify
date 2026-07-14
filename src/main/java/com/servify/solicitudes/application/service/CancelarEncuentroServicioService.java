package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.CancelarEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.port.in.CancelarEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoEncuentroServicio;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.ServicioEncuentro;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.CalculadorFechasRecurrencia;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CancelarEncuentroServicioService implements CancelarEncuentroServicioUseCase {

    private final ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;
    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;
    private final CalculadorFechasRecurrencia calculadorFechasRecurrencia;

    public CancelarEncuentroServicioService(ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                            NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this(servicioEncuentroRepositoryPort, solicitudServicioRepositoryPort,
                asignacionServicioRepositoryPort, notificadorEventosSolicitudService,
                null, new CalculadorFechasRecurrencia());
    }

    public CancelarEncuentroServicioService(ServicioEncuentroRepositoryPort servicioEncuentroRepositoryPort,
                                            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
                                            NotificadorEventosSolicitudService notificadorEventosSolicitudService,
                                            ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort,
                                            CalculadorFechasRecurrencia calculadorFechasRecurrencia) {
        this.servicioEncuentroRepositoryPort = servicioEncuentroRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
        this.calculadorFechasRecurrencia = calculadorFechasRecurrencia != null
                ? calculadorFechasRecurrencia
                : new CalculadorFechasRecurrencia();
    }

    @Override
    @Transactional
    public ServicioEncuentroResult cancelar(CancelarEncuentroServicioCommand command) {
        validarCommand(command);
        ServicioEncuentro referencia = servicioEncuentroRepositoryPort.buscarPorId(command.getEncuentroId())
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado: " + command.getEncuentroId()));
        ServicioRecurrencia recurrenciaBloqueada = bloquearRecurrenciaSiCorresponde(referencia);
        ServicioEncuentro encuentro = obtenerEncuentro(command.getEncuentroId());
        SolicitudServicio solicitud = obtenerSolicitud(encuentro.getSolicitudId());
        AsignacionServicio asignacion = obtenerAsignacion(encuentro);
        validarParticipante(solicitud, asignacion, command.getUsuarioId());

        encuentro.cancelar(LocalDateTime.now());
        ServicioEncuentro guardado = servicioEncuentroRepositoryPort.guardar(encuentro);
        if (esEncuentroPrincipalProgramado(solicitud, guardado)) {
            cancelarSolicitudProgramada(solicitud, asignacion);
        } else {
            continuarProgramaSiCorresponde(solicitud, asignacion, guardado, recurrenciaBloqueada);
        }
        notificarCancelacion(solicitud, asignacion, guardado, command.getUsuarioId());
        return ServicioAgendaMapper.toEncuentroResult(guardado);
    }

    private void validarCommand(CancelarEncuentroServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getEncuentroId() == null) {
            throw new IllegalArgumentException("encuentroId no puede ser nulo");
        }
        if (command.getUsuarioId() == null) {
            throw new IllegalArgumentException("usuarioId no puede ser nulo");
        }
    }

    private ServicioEncuentro obtenerEncuentro(UUID encuentroId) {
        return servicioEncuentroRepositoryPort.buscarPorIdParaActualizar(encuentroId)
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado: " + encuentroId));
    }

    private ServicioRecurrencia bloquearRecurrenciaSiCorresponde(ServicioEncuentro encuentro) {
        if (servicioRecurrenciaRepositoryPort == null || encuentro.getRecurrenciaServicioId() == null) return null;
        return servicioRecurrenciaRepositoryPort.buscarPorIdParaActualizar(encuentro.getRecurrenciaServicioId())
                .orElseThrow(() -> new IllegalStateException("La recurrencia del encuentro ya no existe"));
    }

    private SolicitudServicio obtenerSolicitud(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    private AsignacionServicio obtenerAsignacion(ServicioEncuentro encuentro) {
        return asignacionServicioRepositoryPort.buscarPorIdParaActualizar(encuentro.getAsignacionServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Asignacion no encontrada: " + encuentro.getAsignacionServicioId()));
    }

    private boolean esEncuentroPrincipalProgramado(SolicitudServicio solicitud, ServicioEncuentro encuentro) {
        return solicitud.esProgramada()
                && encuentro.getRecurrenciaServicioId() == null
                && Objects.equals(solicitud.getFechaProgramadaInicio(), encuentro.getFechaInicio())
                && Objects.equals(solicitud.getFechaProgramadaFin(), encuentro.getFechaFin());
    }

    private void cancelarSolicitudProgramada(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        LocalDateTime fechaCancelacion = LocalDateTime.now();
        servicioEncuentroRepositoryPort.buscarPorSolicitudId(solicitud.getId()).stream()
                .filter(encuentro -> !encuentro.estaCerrado())
                .forEach(encuentro -> {
                    encuentro.cancelar(fechaCancelacion);
                    servicioEncuentroRepositoryPort.guardar(encuentro);
                });
        if (!asignacion.estaCancelada() && !asignacion.estaFinalizada()) {
            asignacion.cancelar();
            asignacionServicioRepositoryPort.guardar(asignacion);
        }
        if (!solicitud.estaCancelada() && !solicitud.estaFinalizada()) {
            solicitud.cancelar();
            solicitudServicioRepositoryPort.guardar(solicitud);
        }
    }

    private void validarParticipante(SolicitudServicio solicitud, AsignacionServicio asignacion, UUID usuarioId) {
        boolean esSolicitante = Objects.equals(solicitud.getSolicitanteId(), usuarioId);
        boolean esPrestador = Objects.equals(asignacion.getPrestadorId(), usuarioId);
        if (!esSolicitante && !esPrestador) {
            throw new IllegalArgumentException("El usuario no participa de esta solicitud");
        }
    }

    private void continuarProgramaSiCorresponde(SolicitudServicio solicitud,
                                                 AsignacionServicio asignacion,
                                                 ServicioEncuentro encuentroCancelado,
                                                 ServicioRecurrencia recurrencia) {
        if (recurrencia == null || !recurrencia.estaActiva()) return;

        Optional<java.time.LocalDate> fechaSiguiente = calculadorFechasRecurrencia
                .siguienteFecha(recurrencia, encuentroCancelado.getFechaInicio().toLocalDate());
        if (fechaSiguiente.isEmpty()) {
            recurrencia.finalizar();
            servicioRecurrenciaRepositoryPort.guardar(recurrencia);
            asignacion.finalizar(LocalDateTime.now());
            solicitud.marcarComoFinalizada();
            asignacionServicioRepositoryPort.guardar(asignacion);
            solicitudServicioRepositoryPort.guardar(solicitud);
            return;
        }

        LocalDateTime inicio = LocalDateTime.of(fechaSiguiente.get(), recurrencia.getHoraDesde());
        boolean yaExiste = servicioEncuentroRepositoryPort.buscarPorSolicitudId(solicitud.getId()).stream()
                .anyMatch(encuentro -> Objects.equals(encuentro.getRecurrenciaServicioId(), recurrencia.getId())
                        && Objects.equals(encuentro.getFechaInicio(), inicio));
        if (yaExiste) return;

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
                LocalDateTime.now()
        );
        servicioEncuentroRepositoryPort.guardar(siguiente);
    }

    private void notificarCancelacion(SolicitudServicio solicitud,
                                      AsignacionServicio asignacion,
                                      ServicioEncuentro encuentro,
                                      UUID canceladoPorId) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.encuentroCancelado(solicitud, asignacion, encuentro, canceladoPorId);
        }
    }
}
