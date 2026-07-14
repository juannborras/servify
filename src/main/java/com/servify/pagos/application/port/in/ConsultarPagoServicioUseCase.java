package com.servify.pagos.application.port.in;

import com.servify.pagos.application.dto.PagoServicioResult;

import java.util.Optional;
import java.util.UUID;

public interface ConsultarPagoServicioUseCase {
    Optional<PagoServicioResult> obtenerEstado(UUID solicitudId, UUID asignacionServicioId,
                                               UUID encuentroId, UUID usuarioId);
}
