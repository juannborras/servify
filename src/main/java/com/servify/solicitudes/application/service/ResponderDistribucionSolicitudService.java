package com.servify.solicitudes.application.service;

import com.servify.solicitudes.application.dto.ResponderDistribucionSolicitudCommand;
import com.servify.solicitudes.application.dto.TipoRespuestaDistribucion;
import com.servify.solicitudes.application.port.in.ResponderDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Acá metí también SolicitudServicioRepositoryPort porque la distribución no debería responderse a ciegas;
   conviene validar que la solicitud asociada siga activa y disponible
 */

public class ResponderDistribucionSolicitudService implements ResponderDistribucionSolicitudUseCase {

    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final NotificadorEventosSolicitudService notificadorEventosSolicitudService;

    public ResponderDistribucionSolicitudService(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort
    ) {
        this(distribucionSolicitudRepositoryPort, solicitudServicioRepositoryPort, null);
    }

    public ResponderDistribucionSolicitudService(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.notificadorEventosSolicitudService = notificadorEventosSolicitudService;
    }

    @Override
    public void responder(ResponderDistribucionSolicitudCommand command) {
        // Responde una distribución: el prestador acepta o rechaza una distribución recibida.
        // - valida pertenencia, estado de la distribución y la solicitud asociada
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getDistribucionSolicitudId() == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        if (command.getPrestadorId() == null) {
            throw new IllegalArgumentException("prestadorId no puede ser nulo");
        }
        if (command.getTipoRespuesta() == null) {
            throw new IllegalArgumentException("tipoRespuesta no puede ser nulo");
        }

        DistribucionSolicitud distribucion = obtenerDistribucionExistente(command.getDistribucionSolicitudId());
        validarPertenenciaPrestador(distribucion, command.getPrestadorId());
        validarRespuestaPermitida(distribucion);
        SolicitudServicio solicitud = validarSolicitudAsociadaActiva(distribucion);

        LocalDateTime fechaRespuesta = obtenerFechaActual();
        aplicarRespuesta(distribucion, command.getTipoRespuesta(), fechaRespuesta);
        persistirDistribucion(distribucion);
        notificarRespuesta(solicitud, distribucion, command.getTipoRespuesta());
    }

    protected DistribucionSolicitud obtenerDistribucionExistente(UUID distribucionSolicitudId) {
        if (distribucionSolicitudId == null) {
            throw new IllegalArgumentException("distribucionSolicitudId no puede ser nulo");
        }
        return this.distribucionSolicitudRepositoryPort.buscarPorId(distribucionSolicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Distribución no encontrada: " + distribucionSolicitudId));
    }

    protected void validarPertenenciaPrestador(DistribucionSolicitud distribucionSolicitud, UUID prestadorId) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("Distribución no puede ser nula");
        }
        if (prestadorId == null) {
            throw new IllegalArgumentException("prestadorId no puede ser nulo");
        }
        if (!distribucionSolicitud.perteneceAPrestador(prestadorId)) {
            throw new IllegalArgumentException("El prestador no es propietario de la distribución");
        }
    }

    protected void validarRespuestaPermitida(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("Distribución no puede ser nula");
        }
        if (distribucionSolicitud.estaCerrada()) {
            throw new IllegalStateException("La distribución ya está cerrada");
        }
        if (distribucionSolicitud.estaExpirada()) {
            throw new IllegalStateException("La distribución está expirada");
        }
        if (!distribucionSolicitud.puedeSerRespondida()) {
            throw new IllegalStateException("La distribución no puede ser respondida en su estado actual");
        }
    }

    protected SolicitudServicio validarSolicitudAsociadaActiva(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("Distribución no puede ser nula");
        }
        UUID solicitudId = distribucionSolicitud.getSolicitudId();
        if (solicitudId == null) {
            throw new IllegalStateException("La distribución no tiene solicitud asociada");
        }
        SolicitudServicio solicitud = this.solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new IllegalStateException("Solicitud asociada no encontrada: " + solicitudId));
        if (!solicitud.puedeRecibirRespuestas()) {
            throw new IllegalStateException("La solicitud asociada no está disponible para recibir respuestas");
        }
        return solicitud;
    }

    protected void aplicarRespuesta(DistribucionSolicitud distribucionSolicitud,
                                    TipoRespuestaDistribucion tipoRespuesta,
                                    LocalDateTime fechaRespuesta) {
        if (tipoRespuesta == null) {
            throw new IllegalArgumentException("tipoRespuesta no puede ser nulo");
        }
        if (TipoRespuestaDistribucion.ACEPTAR.equals(tipoRespuesta)) {
            distribucionSolicitud.aceptar(fechaRespuesta);
        } else if (TipoRespuestaDistribucion.RECHAZAR.equals(tipoRespuesta)) {
            distribucionSolicitud.rechazar(fechaRespuesta);
        } else {
            throw new IllegalArgumentException("Tipo de respuesta no soportado: " + tipoRespuesta);
        }
    }

    protected void persistirDistribucion(DistribucionSolicitud distribucionSolicitud) {
        if (distribucionSolicitud == null) {
            throw new IllegalArgumentException("Distribución no puede ser nula");
        }
        this.distribucionSolicitudRepositoryPort.guardar(distribucionSolicitud);
    }

    protected void notificarRespuesta(
            SolicitudServicio solicitud,
            DistribucionSolicitud distribucion,
            TipoRespuestaDistribucion tipoRespuesta
    ) {
        if (notificadorEventosSolicitudService != null) {
            notificadorEventosSolicitudService.respuestaPrestador(solicitud, distribucion, tipoRespuesta);
        }
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }
}
