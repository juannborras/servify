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
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class NotificadorEventosSolicitudService {

    private static final DateTimeFormatter FECHA_NOTIFICACION = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort;

    public NotificadorEventosSolicitudService(CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase) {
        this(crearNotificacionUsuarioUseCase, null, null);
    }

    public NotificadorEventosSolicitudService(
            CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase,
            UsuarioRepositoryPort usuarioRepositoryPort,
            PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort
    ) {
        this.crearNotificacionUsuarioUseCase = crearNotificacionUsuarioUseCase;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.perfilUsuarioRepositoryPort = perfilUsuarioRepositoryPort;
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
        String prestador = nombreUsuario(distribucion.getPrestadorId());
        String accion = respuesta == TipoRespuestaDistribucion.ACEPTAR ? "acepto" : "rechazo";
        crear(
                solicitud.getSolicitanteId(),
                TipoNotificacion.SOLICITUD_RESPONDIDA,
                respuesta == TipoRespuestaDistribucion.ACEPTAR ? prestador + " acepto tu solicitud" : prestador + " rechazo tu solicitud",
                resumenSolicitud(prestador + " " + accion + " tu solicitud.", solicitud),
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
                resumenSolicitud("Contraoferta de " + nombreUsuario(contraoferta.getPrestadorId()) + " " + precio(contraoferta.getPrecioPropuesto()) + comentario(contraoferta.getMensaje()), solicitud),
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
                        ? nombreUsuario(solicitud.getSolicitanteId()) + " acepto tu contraoferta " + precio(contraoferta.getPrecioPropuesto()) + "."
                        : nombreUsuario(solicitud.getSolicitanteId()) + " rechazo tu contraoferta y seguira buscando.", solicitud),
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
                resumenSolicitud(nombreUsuario(solicitud.getSolicitanteId()) + " confirmo tu propuesta. El servicio quedo asignado.", solicitud),
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
                        resumenSolicitud(nombreUsuario(solicitud.getSolicitanteId()) + " cancelo una solicitud que tenias asociada.", solicitud),
                        "SOLICITUD",
                        solicitud.getId()
                ));
    }

    public void confirmacionFinalizacion(SolicitudServicio solicitud, AsignacionServicio asignacion, RolConfirmante rolConfirmante) {
        if (solicitud == null || asignacion == null || rolConfirmante == null) return;
        UUID destinatario = rolConfirmante == RolConfirmante.SOLICITANTE
                ? asignacion.getPrestadorId()
                : solicitud.getSolicitanteId();
        UUID confirmante = rolConfirmante == RolConfirmante.SOLICITANTE
                ? solicitud.getSolicitanteId()
                : asignacion.getPrestadorId();
        crear(
                destinatario,
                TipoNotificacion.SERVICIO_FINALIZACION,
                "Confirmacion de finalizacion",
                resumenSolicitud(nombreUsuario(confirmante) + " confirmo la finalizacion del servicio.", solicitud),
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
                + ". Solicitante: " + nombreUsuario(solicitud.getSolicitanteId()) + ".";
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

    private String nombreUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            return "sin dato";
        }

        String nombrePerfil = nombrePerfil(usuarioId);
        if (nombrePerfil != null && !nombrePerfil.isBlank()) {
            return nombrePerfil;
        }

        if (usuarioRepositoryPort != null) {
            return usuarioRepositoryPort.buscarPorId(usuarioId)
                    .map(Usuario::getNombreUsuario)
                    .filter(nombre -> nombre != null && !nombre.isBlank())
                    .orElse(shortId(usuarioId));
        }

        return shortId(usuarioId);
    }

    private String nombrePerfil(UUID usuarioId) {
        if (perfilUsuarioRepositoryPort == null) {
            return null;
        }

        return perfilUsuarioRepositoryPort.buscarPorUsuarioId(usuarioId)
                .map(this::nombreMostrar)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .orElse(null);
    }

    private String nombreMostrar(PerfilUsuario perfilUsuario) {
        if (perfilUsuario == null || perfilUsuario.getNombreCompleto() == null) {
            return null;
        }
        return perfilUsuario.getNombreCompleto().nombreMostrar().trim();
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
