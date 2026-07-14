package com.servify.pagos.infrastructure.mercadopago;

import com.servify.shared.domain.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoWebhookSignatureValidatorTests {

    @Test
    void validaElManifestOficialYRechazaUnaFirmaAlterada() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setWebhookSecret("secret-demo");
        MercadoPagoWebhookSignatureValidator validator = new MercadoPagoWebhookSignatureValidator(properties);
        String manifest = "id:12345;request-id:req-abc;ts:1704908010;";
        String signature = "ts=1704908010,v1="
                + MercadoPagoWebhookSignatureValidator.hmacHex(manifest, "secret-demo");

        assertDoesNotThrow(() -> validator.validar(signature, "req-abc", "12345"));
        assertThrows(UnauthorizedException.class,
                () -> validator.validar(signature + "0", "req-abc", "12345"));
    }

    @Test
    void webhookSinSecretNoQuedaExpuesto() {
        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(new MercadoPagoProperties());
        assertThrows(UnauthorizedException.class,
                () -> validator.validar("ts=1,v1=abc", "req", "1"));
    }

    @Test
    void soloUsaBackUrlsHttpsPublicas() {
        assertTrue(MercadoPagoGatewayAdapter.esUrlPublicaHttps("https://servify.app/pago"));
        assertFalse(MercadoPagoGatewayAdapter.esUrlPublicaHttps("http://localhost:5173"));
        assertFalse(MercadoPagoGatewayAdapter.esUrlPublicaHttps("https://127.0.0.1/pago"));
    }

    @Test
    void noOcultaUnCobroAprobadoPorUnReintentoPosteriorRechazado() {
        var rechazadoNuevo = new com.servify.pagos.application.port.out.MercadoPagoGatewayPort.PagoExterno(
                "2", "rejected", "ref", BigDecimal.TEN, "ARS", null, "cc_rejected");
        var aprobadoAnterior = new com.servify.pagos.application.port.out.MercadoPagoGatewayPort.PagoExterno(
                "1", "approved", "ref", BigDecimal.TEN, "ARS", null, "accredited");

        var seleccionado = MercadoPagoGatewayAdapter
                .seleccionarPago(List.of(rechazadoNuevo, aprobadoAnterior))
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals("1", seleccionado.paymentId());
    }
}
