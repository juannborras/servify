package com.servify.pagos.application.dto;

import java.util.UUID;

public record SincronizarPagoServicioCommand(
        UUID pagoId,
        UUID solicitanteId,
        String mercadoPagoPaymentId
) {}
