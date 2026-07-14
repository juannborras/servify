package com.servify.pagos.application.dto;

import java.util.UUID;

public record IniciarPagoServicioCommand(
        UUID solicitudId,
        UUID solicitanteId,
        UUID asignacionServicioId,
        UUID encuentroId
) {}
