package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.EmitirContraofertaCommand;
import com.servify.solicitudes.application.port.in.EmitirContraofertaUseCase;
import com.servify.solicitudes.application.port.out.ContraofertaRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoContraoferta;
import com.servify.solicitudes.domain.model.Contraoferta;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmitirContraofertaService implements EmitirContraofertaUseCase {

    private final ContraofertaRepositoryPort contraofertaRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public EmitirContraofertaService(ContraofertaRepositoryPort contraofertaRepositoryPort,
                                     DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                     SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this(contraofertaRepositoryPort, distribucionSolicitudRepositoryPort, solicitudServicioRepositoryPort, null);
    }

    public EmitirContraofertaService(ContraofertaRepositoryPort contraofertaRepositoryPort,
                                     DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                     SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                     NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.contraofertaRepositoryPort = contraofertaRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public void emitir(EmitirContraofertaCommand command) {
        // Emite una contraoferta para una distribución de solicitud.
        // - Valida el comando y la pertenencia del prestador.
        // - Verifica que la distribución y la solicitud asociada estén en estado aceptable.
        // - Crea la entidad `Contraoferta`, marca la distribución y persiste ambos cambios.
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getDistribucionSolicitudId() == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        if (command.getPrestadorId() == null) {
            throw new IllegalArgumentException("prestadorId no puede ser nulo");
        }
        if (command.getPrecioPropuesto() == null) {
            throw new IllegalArgumentException("precioPropuesto no puede ser nulo");
        }

        DistribucionSolicitud distribucion = obtenerDistribucionExistente(command.getDistribucionSolicitudId());
        validarPertenenciaPrestador(distribucion, command.getPrestadorId());
        validarContraofertaPermitida(distribucion);
        SolicitudServicio solicitud = validarSolicitudAsociadaActiva(distribucion);
        validarAusenciaDeContraofertaPendiente(distribucion.getId());

        LocalDateTime ahora = obtenerFechaActual();
        Contraoferta contraoferta = construirContraoferta(command, distribucion, ahora);

        marcarDistribucionComoContraofertada(distribucion, ahora);

        persistirContraoferta(contraoferta);
        persistirDistribucion(distribucion);
        notificarContraoferta(solicitud, contraoferta);
    }

    protected DistribucionSolicitud obtenerDistribucionExistente(UUID distribucionSolicitudId) {
        if (distribucionSolicitudId == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        return this.distribucionSolicitudRepositoryPort.buscarPorId(distribucionSolicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Distribución no encontrada: " + distribucionSolicitudId));
    }

    protected void validarPertenenciaPrestador(DistribucionSolicitud distribucionSolicitud, UUID prestadorId) {
        if (distribucionSolicitud == null || prestadorId == null) {
            throw new IllegalArgumentException("Argumentos inválidos para validar pertenencia de prestador");
        }
        if (!distribucionSolicitud.perteneceAPrestador(prestadorId)) {
            throw new IllegalArgumentException("La distribución no pertenece al prestador indicado");
        }
    }

    protected void validarContraofertaPermitida(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("distribucionSolicitud no puede ser nula");
        }
        if (distribucionSolicitud.estaCerrada()) {
            throw new IllegalStateException("La distribución está cerrada");
        }
        if (distribucionSolicitud.estaExpirada()) {
            throw new IllegalStateException("La distribución está expirada");
        }
        if (distribucionSolicitud.fueRespondida()) {
            throw new IllegalStateException("La distribución ya fue respondida definitivamente");
        }
        if (!distribucionSolicitud.puedeSerRespondida()) {
            throw new IllegalStateException("La distribución no está en un estado apto para recibir contraoferta");
        }
    }

    protected SolicitudServicio validarSolicitudAsociadaActiva(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("distribucionSolicitud no puede ser nula");
        }
        SolicitudServicio solicitud = this.solicitudServicioRepositoryPort.buscarPorId(distribucionSolicitud.getSolicitudId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud asociada no encontrada: " + distribucionSolicitud.getSolicitudId()));
        if (!solicitud.puedeRecibirRespuestas()) {
            throw new IllegalStateException("La solicitud asociada no está activa o ya no acepta respuestas");
        }
        return solicitud;
    }

    protected void validarAusenciaDeContraofertaPendiente(UUID distribucionSolicitudId) {
        if (distribucionSolicitudId == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        this.contraofertaRepositoryPort.buscarPendientePorDistribucionSolicitudId(distribucionSolicitudId)
                .ifPresent(c -> {
                    throw new IllegalStateException("Ya existe una contraoferta pendiente para esta distribución: " + c.getId());
                });
    }

    protected Contraoferta construirContraoferta(EmitirContraofertaCommand command,
                                                 DistribucionSolicitud distribucionSolicitud,
                                                 LocalDateTime fechaEmision) {
        if (command == null || distribucionSolicitud == null || fechaEmision == null) {
            throw new IllegalArgumentException("Argumentos inválidos para construir contraoferta");
        }
        BigDecimal precioOriginal = this.solicitudServicioRepositoryPort
            .buscarPorId(distribucionSolicitud.getSolicitudId())
            .map(s -> s.getPrecioReferencia())
            .orElse(BigDecimal.ZERO);

        return new Contraoferta(
            generarIdContraoferta(),
            distribucionSolicitud.getId(),
            command.getPrestadorId(),
            precioOriginal,
            command.getPrecioPropuesto(),
            command.getMensaje(),
            obtenerEstadoInicialContraoferta(),
            fechaEmision,
            null
        );
    }

    protected void marcarDistribucionComoContraofertada(DistribucionSolicitud distribucionSolicitud,
                                                        LocalDateTime fechaRespuesta) {
        if (distribucionSolicitud == null || fechaRespuesta == null) {
            throw new IllegalArgumentException("Argumentos inválidos para marcar distribución como contraofertada");
        }
        distribucionSolicitud.marcarComoContraofertada(fechaRespuesta);
    }

    protected void persistirContraoferta(Contraoferta contraoferta) {
        if (contraoferta == null) {
            throw new IllegalArgumentException("contraoferta no puede ser nula");
        }
        this.contraofertaRepositoryPort.guardar(contraoferta);
    }

    protected void persistirDistribucion(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("distribucionSolicitud no puede ser nula");
        }
        this.distribucionSolicitudRepositoryPort.guardar(distribucionSolicitud);
    }

    protected void notificarContraoferta(SolicitudServicio solicitud, Contraoferta contraoferta) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.contraofertaRecibida(solicitud, contraoferta);
        }
    }

    protected EstadoContraoferta obtenerEstadoInicialContraoferta() {
        return EstadoContraoferta.PENDIENTE;
    }

    protected UUID generarIdContraoferta() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }
}
