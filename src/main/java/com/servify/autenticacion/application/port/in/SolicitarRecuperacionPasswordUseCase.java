package com.servify.autenticacion.application.port.in;

import com.servify.autenticacion.application.dto.RecuperacionPasswordResult;
import com.servify.autenticacion.application.dto.SolicitarRecuperacionPasswordCommand;

public interface SolicitarRecuperacionPasswordUseCase {

    RecuperacionPasswordResult solicitar(SolicitarRecuperacionPasswordCommand command);
}
