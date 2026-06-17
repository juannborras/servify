package com.servify.chat.application.port.in;

import com.servify.chat.application.dto.MensajeChatResult;

import java.util.List;
import java.util.UUID;

public interface ListarMensajesChatUseCase {

    List<MensajeChatResult> listar(UUID solicitudId, UUID prestadorId, UUID usuarioId);
}
