package com.servify.solicitudes.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AcordarPrecioAsignacionCommand {

    private final UUID solicitudId;
    private final UUID asignacionServicioId;
    private final UUID solicitanteId;
    private final BigDecimal precioAcordado;

    public AcordarPrecioAsignacionCommand(UUID solicitudId,
                                          UUID asignacionServicioId,
                                          UUID solicitanteId,
                                          BigDecimal precioAcordado) {
        this.solicitudId = solicitudId;
        this.asignacionServicioId = asignacionServicioId;
        this.solicitanteId = solicitanteId;
        this.precioAcordado = precioAcordado;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getAsignacionServicioId() {
        return asignacionServicioId;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public BigDecimal getPrecioAcordado() {
        return precioAcordado;
    }
}
