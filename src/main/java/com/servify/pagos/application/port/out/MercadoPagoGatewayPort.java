package com.servify.pagos.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MercadoPagoGatewayPort {

    PreferenciaCreada crearPreferencia(SolicitudPreferencia solicitud);

    PagoExterno obtenerPago(String paymentId);

    Optional<PagoExterno> buscarPagoPorExternalReference(String externalReference);

    record SolicitudPreferencia(
            UUID pagoId,
            UUID solicitudId,
            UUID encuentroId,
            String externalReference,
            String titulo,
            BigDecimal monto,
            String moneda
    ) {}

    record PreferenciaCreada(String preferenceId, String checkoutUrl) {}

    record PagoExterno(
            String paymentId,
            String estado,
            String externalReference,
            BigDecimal monto,
            String moneda,
            LocalDateTime fechaAprobacion,
            String detalleEstado
    ) {}
}
