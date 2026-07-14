package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ContraofertaResult;
import com.servify.solicitudes.application.dto.ResolverContraofertaCommand;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.port.in.ResolverContraofertaUseCase;
import com.servify.solicitudes.application.port.out.ContraofertaRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.Contraoferta;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResolverContraofertaService implements ResolverContraofertaUseCase {

    private final ContraofertaRepositoryPort contraofertaRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public ResolverContraofertaService(ContraofertaRepositoryPort contraofertaRepositoryPort,
                                       DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                       SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this(contraofertaRepositoryPort, distribucionSolicitudRepositoryPort, solicitudServicioRepositoryPort, null);
    }

    public ResolverContraofertaService(ContraofertaRepositoryPort contraofertaRepositoryPort,
                                       DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                       SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                       NotificadorEventosSolicitudService notificadorEventosSolicitudService) {
        this.contraofertaRepositoryPort = contraofertaRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public ContraofertaResult resolver(ResolverContraofertaCommand command) {
        // Resuelve una contraoferta emitida por un prestador.
        // - valida permisos del solicitante
        // - aplica decisión (aceptar/rechazar) y actualiza estados relacionados
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getContraofertaId() == null) {
            throw new IllegalArgumentException("contraofertaId no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        if (command.getDecision() == null) {
            throw new IllegalArgumentException("decision no puede ser nula");
        }

        Contraoferta contraoferta = obtenerContraofertaExistente(command.getContraofertaId());
        DistribucionSolicitud distribucion = obtenerDistribucionExistente(contraoferta.getDistribucionSolicitudId());
        SolicitudServicio solicitud = obtenerSolicitudExistente(distribucion.getSolicitudId());

        validarSolicitantePuedeResolver(solicitud, command.getSolicitanteId());

        if (!contraoferta.puedeSerResuelta()) {
            throw new IllegalStateException("La contraoferta no puede ser resuelta");
        }

        LocalDateTime ahora = obtenerFechaActual();
        aplicarDecision(contraoferta, distribucion, command.getDecision(), ahora);

        this.contraofertaRepositoryPort.guardar(contraoferta);
        // persistir distribucion si cambió su estado
        this.distribucionSolicitudRepositoryPort.guardar(distribucion);
        if (command.getDecision() == TipoDecisionSolicitud.ACEPTAR) {
            solicitud.actualizarPrecioReferencia(contraoferta.getPrecioPropuesto());
            this.solicitudServicioRepositoryPort.guardar(solicitud);
        }
        notificarResolucion(solicitud, contraoferta, command.getDecision());

        return construirResultado(contraoferta);
    }

    protected Contraoferta obtenerContraofertaExistente(UUID contraofertaId) {
        if (contraofertaId == null) {
            throw new IllegalArgumentException("contraofertaId no puede ser nulo");
        }
        return this.contraofertaRepositoryPort.buscarPorId(contraofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Contraoferta no encontrada: " + contraofertaId));
    }

    protected DistribucionSolicitud obtenerDistribucionExistente(UUID distribucionSolicitudId) {
        if (distribucionSolicitudId == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        return this.distribucionSolicitudRepositoryPort.buscarPorId(distribucionSolicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Distribución no encontrada: " + distribucionSolicitudId));
    }

    protected SolicitudServicio obtenerSolicitudExistente(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException("solicitudId no puede ser nulo");
        }
        return this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + solicitudId));
    }

    protected void validarSolicitantePuedeResolver(SolicitudServicio solicitudServicio,
                                                   UUID solicitanteId) {
        if (solicitudServicio == null) {
            throw new IllegalArgumentException("Solicitud no puede ser nula");
        }
        if (solicitanteId == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        if (!solicitudServicio.getSolicitanteId().equals(solicitanteId)) {
            throw new IllegalArgumentException("El solicitante no está autorizado para resolver la contraoferta");
        }
    }

    protected void aplicarDecision(Contraoferta contraoferta,
                                   DistribucionSolicitud distribucionSolicitud,
                                   TipoDecisionSolicitud decision,
                                   LocalDateTime fechaResolucion) {
        if (contraoferta == null || distribucionSolicitud == null || decision == null) {
            throw new IllegalArgumentException("Argumentos inválidos para aplicar decisión");
        }
        if (decision == TipoDecisionSolicitud.ACEPTAR) {
            contraoferta.aceptar(fechaResolucion);
            // marcar la distribución como aceptada para permitir asignación
            distribucionSolicitud.aceptarContraoferta(fechaResolucion);
        } else if (decision == TipoDecisionSolicitud.RECHAZAR) {
            contraoferta.rechazar(fechaResolucion);
            distribucionSolicitud.rechazarContraoferta(fechaResolucion);
            // dejar la distribución en su estado previo (normalmente ENVIADA)
        } else {
            throw new IllegalArgumentException("Decisión no soportada: " + decision);
        }
    }

    protected ContraofertaResult construirResultado(Contraoferta contraoferta) {
        if (contraoferta == null) {
            return null;
        }
        return ContraofertaResult.builder()
                .id(contraoferta.getId())
                .distribucionSolicitudId(contraoferta.getDistribucionSolicitudId())
                .prestadorId(contraoferta.getPrestadorId())
                .precioOriginal(contraoferta.getPrecioOriginal())
                .precioPropuesto(contraoferta.getPrecioPropuesto())
                .mensaje(contraoferta.getMensaje())
                .estado(contraoferta.getEstado())
                .fechaEmision(contraoferta.getFechaEmision())
                .fechaResolucion(contraoferta.getFechaResolucion())
                .build();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    protected void notificarResolucion(
            SolicitudServicio solicitud,
            Contraoferta contraoferta,
            TipoDecisionSolicitud decision
    ) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.contraofertaResuelta(solicitud, contraoferta, decision);
        }
    }
}
