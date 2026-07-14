package com.servify.pagos.application.port.in;

import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.dto.SincronizarPagoServicioCommand;

public interface SincronizarPagoServicioUseCase {
    PagoServicioResult sincronizar(SincronizarPagoServicioCommand command);
    void sincronizarWebhook(String mercadoPagoPaymentId);
}
