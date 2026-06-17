package com.servify.solicitudes.application.service;

import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.dto.TipoRespuestaDistribucion;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.Contraoferta;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class NotificadorEventosSolicitudService {

    private static final DateTimeFormatter FECHA_NOTIFICACION = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase;

    public NotificadorEventosSolicitudService(CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase) {
        this.crearNotificacionUsuarioUseCase = crearNotificacionUsuarioUseCase;
    }

    public void nuevaSolicitudCompatible(SolicitudServicio solicitud, List<DistribucionSolicitud> distribuciones) {
        if (solicitud == null || distribuciones == null || distribuciones.isEmpty()) return;
        distribuciones.stream()
                .filter(distribucion -> distribucion != null && distribucion.getPrestadorId() != null)
                .forEach(distribucion -> crear(
                        distribucion.getPrestadorId(),
                        TipoNotificacion.SOLICITUD_COMPATIBLE,
                        "Nueva solicitud compatible",
                        resumenSolicitud("Tenes una solicitud compatible para revisar.", solicitud),
                        "SOLICITUD",
                        solicitud.getId()
                ));
    }

    public void respuestaPrestador(SolicitudServicio solicitud, DistribucionSolicitud distribucion, TipoRespuestaDistribucion respuesta) {
        if (solicitud == null || distribucion == null || respuesta == null) return;
        String accion = respuesta == TipoRespuestaDistribucion.ACEPTAR ? "acepto" : "rechazo";
        crear(
                solicitud.getSolicitanteId(),
                TipoNotificacion.SOLICITUD_RESPONDIDA,
                respuesta == TipoRespuestaDistribucion.ACEPTAR ? "Un prestador acepto tu solicitud" : "Un prestador rechazo tu solicitud",
                resumenSolicitud("Un prestador " + accion + " tu solicitud.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void contraofertaRecibida(SolicitudServicio solicitud, Contraoferta contraoferta) {
        if (solicitud == null || contraoferta == null) return;
        crear(
                solicitud.getSolicitanteId(),
                TipoNotificacion.CONTRAOFERTA_RECIBIDA,
                "Recibiste una contraoferta",
                resumenSolicitud("Contraoferta " + precio(contraoferta.getPrecioPropuesto()) + comentario(contraoferta.getMensaje()), solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void contraofertaResuelta(SolicitudServicio solicitud, Contraoferta contraoferta, TipoDecisionSolicitud decision) {
        if (solicitud == null || contraoferta == null || decision == null) return;
        boolean aceptada = decision == TipoDecisionSolicitud.ACEPTAR;
        crear(
                contraoferta.getPrestadorId(),
                TipoNotificacion.CONTRAOFERTA_RESUELTA,
                aceptada ? "Tu contraoferta fue aceptada" : "Tu contraoferta fue rechazada",
                resumenSolicitud(aceptada
                        ? "Contraoferta aceptada " + precio(contraoferta.getPrecioPropuesto()) + "."
                        : "Contraoferta rechazada. El solicitante seguira buscando.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void servicioAsignado(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        if (solicitud == null || asignacion == null) return;
        crear(
                asignacion.getPrestadorId(),
                TipoNotificacion.SERVICIO_ASIGNADO,
                "Servicio asignado",
                resumenSolicitud("El solicitante confirmo tu propuesta. El servicio quedo asignado.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void solicitudCancelada(SolicitudServicio solicitud, List<DistribucionSolicitud> distribuciones) {
        if (solicitud == null || distribuciones == null || distribuciones.isEmpty()) return;
        distribuciones.stream()
                .filter(distribucion -> distribucion != null && distribucion.getPrestadorId() != null)
                .map(DistribucionSolicitud::getPrestadorId)
                .distinct()
                .forEach(prestadorId -> crear(
                        prestadorId,
                        TipoNotificacion.SOLICITUD_CANCELADA,
                        "Solicitud cancelada",
                        resumenSolicitud("El solicitante cancelo una solicitud que tenias asociada.", solicitud),
                        "SOLICITUD",
                        solicitud.getId()
                ));
    }

    public void confirmacionFinalizacion(SolicitudServicio solicitud, AsignacionServicio asignacion, RolConfirmante rolConfirmante) {
        if (solicitud == null || asignacion == null || rolConfirmante == null) return;
        UUID destinatario = rolConfirmante == RolConfirmante.SOLICITANTE
                ? asignacion.getPrestadorId()
                : solicitud.getSolicitanteId();
        crear(
                destinatario,
                TipoNotificacion.SERVICIO_FINALIZACION,
                "Confirmacion de finalizacion",
                resumenSolicitud("La otra parte confirmo la finalizacion del servicio.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void servicioFinalizado(SolicitudServicio solicitud, AsignacionServicio asignacion) {
        if (solicitud == null || asignacion == null) return;
        crear(
                solicitud.getSolicitanteId(),
                TipoNotificacion.SERVICIO_FINALIZACION,
                "Servicio finalizado",
                resumenSolicitud("El servicio quedo finalizado por ambas partes.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
        crear(
                asignacion.getPrestadorId(),
                TipoNotificacion.SERVICIO_FINALIZACION,
                "Servicio finalizado",
                resumenSolicitud("El servicio quedo finalizado por ambas partes.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    public void calificacionRecibida(UUID calificadoId, SolicitudServicio solicitud, UUID asignacionId) {
        if (calificadoId == null || solicitud == null || asignacionId == null) return;
        crear(
                calificadoId,
                TipoNotificacion.CALIFICACION_RECIBIDA,
                "Recibiste una calificacion",
                resumenSolicitud("La otra parte califico el servicio finalizado.", solicitud),
                "SOLICITUD",
                solicitud.getId()
        );
    }

    private void crear(
            UUID usuarioId,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            String referenciaTipo,
            UUID referenciaId
    ) {
        if (usuarioId == null || crearNotificacionUsuarioUseCase == null) return;
        crearNotificacionUsuarioUseCase.crear(new CrearNotificacionUsuarioCommand(
                usuarioId,
                tipo,
                titulo,
                mensaje,
                referenciaTipo,
                referenciaId
        ));
    }

    private String resumenSolicitud(String prefijo, SolicitudServicio solicitud) {
        String descripcion = solicitud.getDescripcionNecesidad();
        String servicio = "Servicio sin descripcion";
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = "";
        } else {
            servicio = descripcion.trim();
        }
        if (servicio.length() > 58) {
            servicio = servicio.substring(0, 55) + "...";
        }
        return prefijo + " Servicio: " + servicio
                + ". Fecha: " + fecha(solicitud)
                + ". Solicitante: " + shortId(solicitud.getSolicitanteId()) + ".";
    }

    private String fecha(SolicitudServicio solicitud) {
        if (solicitud.getFechaSolicitud() == null) {
            return "sin fecha";
        }
        return solicitud.getFechaSolicitud().format(FECHA_NOTIFICACION);
    }

    private String shortId(UUID id) {
        if (id == null) {
            return "sin dato";
        }
        String value = id.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    private String precio(BigDecimal value) {
        if (value == null) {
            return "a convenir";
        }
        return "$" + value.stripTrailingZeros().toPlainString();
    }

    private String comentario(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return ".";
        }
        String limpio = mensaje.trim();
        if (limpio.length() > 70) {
            limpio = limpio.substring(0, 67) + "...";
        }
        return ". Mensaje: " + limpio + ".";
    }
}
