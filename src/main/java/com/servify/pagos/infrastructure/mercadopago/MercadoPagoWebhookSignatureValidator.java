package com.servify.pagos.infrastructure.mercadopago;

import com.servify.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

@Component
public class MercadoPagoWebhookSignatureValidator {
    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookSignatureValidator(MercadoPagoProperties properties) {
        this.properties = properties;
    }

    public void validar(String xSignature, String xRequestId, String dataId) {
        if (!properties.tieneWebhookSecret()) {
            throw new UnauthorizedException("El webhook de Mercado Pago no esta habilitado sin SERVIFY_MERCADOPAGO_WEBHOOK_SECRET");
        }
        if (xSignature == null || xRequestId == null || dataId == null) {
            throw new UnauthorizedException("Notificacion de Mercado Pago sin firma completa");
        }
        String ts = valorFirma(xSignature, "ts");
        String recibida = valorFirma(xSignature, "v1");
        if (ts == null || recibida == null) {
            throw new UnauthorizedException("Firma de Mercado Pago invalida");
        }
        String manifest = "id:" + dataId.trim().toLowerCase(Locale.ROOT)
                + ";request-id:" + xRequestId.trim() + ";ts:" + ts + ";";
        String esperada = hmacHex(manifest, properties.getWebhookSecret().trim());
        if (!MessageDigest.isEqual(esperada.getBytes(StandardCharsets.US_ASCII),
                recibida.getBytes(StandardCharsets.US_ASCII))) {
            throw new UnauthorizedException("Firma de Mercado Pago invalida");
        }
    }

    private static String valorFirma(String header, String key) {
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(part -> part.startsWith(key + "="))
                .map(part -> part.substring(key.length() + 1))
                .findFirst().orElse(null);
    }

    static String hmacHex(String manifest, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo validar la firma de Mercado Pago", exception);
        }
    }
}
