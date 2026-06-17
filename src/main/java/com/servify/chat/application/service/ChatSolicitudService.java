package com.servify.chat.application.service;

import com.servify.chat.application.dto.EnviarMensajeChatCommand;
import com.servify.chat.application.dto.MensajeChatResult;
import com.servify.chat.application.port.in.EnviarMensajeChatUseCase;
import com.servify.chat.application.port.in.ListarMensajesChatUseCase;
import com.servify.chat.application.port.out.MensajeChatRepositoryPort;
import com.servify.chat.domain.model.MensajeChat;
import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.shared.domain.exception.NotFoundException;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChatSolicitudService implements EnviarMensajeChatUseCase, ListarMensajesChatUseCase {

    private final MensajeChatRepositoryPort mensajeChatRepositoryPort;
    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort;
    private final AsignacionServicioRepositoryPort asignacionServicioRepositoryPort;
    private final CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase;

    public ChatSolicitudService(
            MensajeChatRepositoryPort mensajeChatRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase
    ) {
        this.mensajeChatRepositoryPort = mensajeChatRepositoryPort;
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribucionSolicitudRepositoryPort = distribucionSolicitudRepositoryPort;
        this.asignacionServicioRepositoryPort = asignacionServicioRepositoryPort;
        this.crearNotificacionUsuarioUseCase = crearNotificacionUsuarioUseCase;
    }

    @Override
    public MensajeChatResult enviar(EnviarMensajeChatCommand command) {
        if (command == null || command.getSolicitudId() == null || command.getPrestadorId() == null || command.getRemitenteId() == null) {
            throw new IllegalArgumentException("solicitudId, prestadorId y remitenteId son obligatorios");
        }

        SolicitudServicio solicitud = obtenerSolicitud(command.getSolicitudId());
        validarChatHabilitado(solicitud, command.getPrestadorId(), command.getRemitenteId());

        MensajeChat mensaje = new MensajeChat(
                UUID.randomUUID(),
                solicitud.getId(),
                solicitud.getSolicitanteId(),
                command.getPrestadorId(),
                command.getRemitenteId(),
                command.getContenido(),
                LocalDateTime.now()
        );
        MensajeChat guardado = mensajeChatRepositoryPort.guardar(mensaje);
        notificarDestinatario(guardado);
        return toResult(guardado);
    }

    @Override
    public List<MensajeChatResult> listar(UUID solicitudId, UUID prestadorId, UUID usuarioId) {
        if (solicitudId == null || prestadorId == null || usuarioId == null) {
            throw new IllegalArgumentException("solicitudId, prestadorId y usuarioId son obligatorios");
        }

        SolicitudServicio solicitud = obtenerSolicitud(solicitudId);
        validarChatHabilitado(solicitud, prestadorId, usuarioId);
        return mensajeChatRepositoryPort.listarPorSolicitudYPrestador(solicitudId, prestadorId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private SolicitudServicio obtenerSolicitud(UUID solicitudId) {
        return solicitudServicioRepositoryPort.buscarPorId(solicitudId)
                .orElseThrow(() -> new NotFoundException("La solicitud no existe"));
    }

    private void validarChatHabilitado(SolicitudServicio solicitud, UUID prestadorId, UUID usuarioId) {
        if (!usuarioId.equals(solicitud.getSolicitanteId()) && !usuarioId.equals(prestadorId)) {
            throw new ForbiddenException("No podes acceder al chat de una solicitud ajena");
        }
        if (!prestadorVinculado(solicitud.getId(), prestadorId)) {
            throw new ForbiddenException("El chat se habilita cuando hay una propuesta o asignacion vinculada");
        }
    }

    private boolean prestadorVinculado(UUID solicitudId, UUID prestadorId) {
        boolean asignado = asignacionServicioRepositoryPort.buscarPorSolicitudId(solicitudId)
                .map(asignacion -> prestadorId.equals(asignacion.getPrestadorId()))
                .orElse(false);
        if (asignado) {
            return true;
        }
        return distribucionSolicitudRepositoryPort.buscarPorSolicitudId(solicitudId)
                .stream()
                .filter(distribucion -> prestadorId.equals(distribucion.getPrestadorId()))
                .anyMatch(this::distribucionHabilitaChat);
    }

    private boolean distribucionHabilitaChat(DistribucionSolicitud distribucion) {
        return distribucion.estaAceptada() || distribucion.estaContraofertada() || distribucion.estaCerrada();
    }

    private void notificarDestinatario(MensajeChat mensaje) {
        if (crearNotificacionUsuarioUseCase == null) {
            return;
        }
        UUID destinatario = mensaje.getRemitenteId().equals(mensaje.getSolicitanteId())
                ? mensaje.getPrestadorId()
                : mensaje.getSolicitanteId();
        crearNotificacionUsuarioUseCase.crear(new CrearNotificacionUsuarioCommand(
                destinatario,
                TipoNotificacion.MENSAJE_CHAT,
                "Nuevo mensaje",
                resumenMensaje(mensaje.getContenido()),
                "SOLICITUD",
                mensaje.getSolicitudId()
        ));
    }

    private String resumenMensaje(String contenido) {
        String texto = contenido == null ? "" : contenido.trim();
        if (texto.length() > 110) {
            texto = texto.substring(0, 107) + "...";
        }
        return "Tenes un nuevo mensaje en una solicitud. " + texto;
    }

    private MensajeChatResult toResult(MensajeChat mensaje) {
        return new MensajeChatResult(
                mensaje.getId(),
                mensaje.getSolicitudId(),
                mensaje.getSolicitanteId(),
                mensaje.getPrestadorId(),
                mensaje.getRemitenteId(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );
    }
}
