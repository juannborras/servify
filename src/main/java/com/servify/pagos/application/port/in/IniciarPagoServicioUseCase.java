package com.servify.pagos.application.port.in;

import com.servify.pagos.application.dto.IniciarPagoServicioCommand;
import com.servify.pagos.application.dto.PagoServicioResult;

public interface IniciarPagoServicioUseCase {
    PagoServicioResult iniciar(IniciarPagoServicioCommand command);
}
