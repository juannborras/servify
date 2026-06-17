package com.servify.chat.application.port.in;

import com.servify.chat.application.dto.EnviarMensajeChatCommand;
import com.servify.chat.application.dto.MensajeChatResult;

public interface EnviarMensajeChatUseCase {

    MensajeChatResult enviar(EnviarMensajeChatCommand command);
}
