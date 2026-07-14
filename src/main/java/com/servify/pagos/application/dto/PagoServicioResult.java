package com.servify.pagos.application.dto;

import com.servify.pagos.domain.enumtype.EstadoPagoServicio;
import com.servify.pagos.domain.model.PagoServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagoServicioResult(
        UUID id,
        UUID solicitudId,
        UUID asignacionServicioId,
        UUID encuentroId,
        EstadoPagoServicio estado,
        BigDecimal monto,
        String moneda,
        String checkoutUrl,
        String mercadoPagoPaymentId,
        LocalDateTime aprobadoEn,
        boolean canConfirmProvider
) {
    public static PagoServicioResult desde(PagoServicio pago) {
        return new PagoServicioResult(
                pago.getId(), pago.getSolicitudId(), pago.getAsignacionServicioId(),
                pago.getEncuentroServicioId(), pago.getEstado(), pago.getMonto(), pago.getMoneda(),
                pago.getCheckoutUrl(), pago.getMercadoPagoPaymentId(), pago.getAprobadoEn(), pago.estaAprobado());
    }
}
