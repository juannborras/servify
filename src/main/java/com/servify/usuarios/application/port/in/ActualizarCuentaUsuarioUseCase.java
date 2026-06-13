package com.servify.usuarios.application.port.in;

import com.servify.usuarios.application.dto.ActualizarCuentaUsuarioCommand;
import com.servify.usuarios.application.dto.UsuarioResult;

public interface ActualizarCuentaUsuarioUseCase {

    UsuarioResult actualizar(ActualizarCuentaUsuarioCommand command);
}
