package com.servify.chat.application.port.out;

import com.servify.chat.domain.model.MensajeChat;

import java.util.List;
import java.util.UUID;

public interface MensajeChatRepositoryPort {

    MensajeChat guardar(MensajeChat mensaje);

    List<MensajeChat> listarPorSolicitudYPrestador(UUID solicitudId, UUID prestadorId);
}
