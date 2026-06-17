package com.servify.autenticacion.application.port.in;

import com.servify.autenticacion.application.dto.RestablecerPasswordCommand;

public interface RestablecerPasswordUseCase {

    void restablecer(RestablecerPasswordCommand command);
}
