package com.servify.pagos.infrastructure.mercadopago;

import tools.jackson.databind.JsonNode;
import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.MercadoPagoGatewayPort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MercadoPagoGatewayAdapter implements MercadoPagoGatewayPort, EstadoIntegracionPagoPort {
    private final MercadoPagoProperties properties;
    private final RestClient restClient;

    public MercadoPagoGatewayAdapter(MercadoPagoProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .build();
    }

    @Override
    public boolean estaHabilitada() {
        return properties.tieneAccessToken();
    }

    @Override
    public boolean esObligatoria() {
        return properties.isRequired();
    }

    @Override
    public PreferenciaCreada crearPreferencia(SolicitudPreferencia solicitud) {
        exigirConfiguracion();
        Optional<PreferenciaCreada> existente = buscarPreferencia(solicitud.externalReference());
        if (existente.isPresent()) return existente.get();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", solicitud.encuentroId() == null
                ? solicitud.solicitudId().toString() : solicitud.encuentroId().toString());
        item.put("title", solicitud.titulo());
        item.put("quantity", 1);
        item.put("currency_id", solicitud.moneda());
        item.put("unit_price", solicitud.monto());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", List.of(item));
        body.put("external_reference", solicitud.externalReference());
        body.put("binary_mode", true);
        agregarUrlsPublicas(body, solicitud);

        try {
            JsonNode response = restClient.post()
                    .uri("/checkout/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getAccessToken().trim())
                    .header("X-Idempotency-Key", solicitud.pagoId().toString())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) throw new IllegalStateException("Mercado Pago devolvio una respuesta vacia");
            String preferenceId = texto(response, "id");
            String checkoutUrl = properties.getAccessToken().trim().startsWith("TEST-")
                    ? primerTexto(response, "sandbox_init_point", "init_point")
                    : primerTexto(response, "init_point", "sandbox_init_point");
            if (preferenceId == null || checkoutUrl == null) {
                throw new IllegalStateException("Mercado Pago no devolvio id o URL de Checkout Pro");
            }
            return new PreferenciaCreada(preferenceId, checkoutUrl);
        } catch (RestClientResponseException exception) {
            throw errorApi("crear la preferencia", exception);
        } catch (RestClientException exception) {
            throw errorConexion("crear la preferencia", exception);
        }
    }

    private Optional<PreferenciaCreada> buscarPreferencia(String externalReference) {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/checkout/preferences/search")
                            .queryParam("external_reference", externalReference).build())
                    .header("Authorization", "Bearer " + properties.getAccessToken().trim())
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) return Optional.empty();
            JsonNode values = response.path("elements").isArray()
                    ? response.path("elements") : response.path("results");
            if (!values.isArray()) return Optional.empty();
            for (JsonNode node : values) {
                if (!externalReference.equals(texto(node, "external_reference"))) continue;
                String id = texto(node, "id");
                String url = properties.getAccessToken().trim().startsWith("TEST-")
                        ? primerTexto(node, "sandbox_init_point", "init_point")
                        : primerTexto(node, "init_point", "sandbox_init_point");
                if (id != null && url != null) return Optional.of(new PreferenciaCreada(id, url));
                if (id != null) return obtenerPreferencia(id);
            }
            return Optional.empty();
        } catch (RestClientResponseException exception) {
            throw errorApi("buscar una preferencia previa", exception);
        } catch (RestClientException exception) {
            throw errorConexion("buscar una preferencia previa", exception);
        }
    }

    private Optional<PreferenciaCreada> obtenerPreferencia(String preferenceId) {
        JsonNode response = restClient.get()
                .uri("/checkout/preferences/{id}", preferenceId)
                .header("Authorization", "Bearer " + properties.getAccessToken().trim())
                .retrieve()
                .body(JsonNode.class);
        if (response == null) return Optional.empty();
        String url = properties.getAccessToken().trim().startsWith("TEST-")
                ? primerTexto(response, "sandbox_init_point", "init_point")
                : primerTexto(response, "init_point", "sandbox_init_point");
        return url == null ? Optional.empty() : Optional.of(new PreferenciaCreada(preferenceId, url));
    }

    @Override
    public PagoExterno obtenerPago(String paymentId) {
        exigirConfiguracion();
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("mercadoPagoPaymentId es obligatorio");
        }
        try {
            JsonNode response = restClient.get()
                    .uri("/v1/payments/{id}", paymentId.trim())
                    .header("Authorization", "Bearer " + properties.getAccessToken().trim())
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) throw new IllegalStateException("Mercado Pago devolvio una respuesta vacia");
            return mapearPago(response);
        } catch (RestClientResponseException exception) {
            throw errorApi("consultar el pago", exception);
        } catch (RestClientException exception) {
            throw errorConexion("consultar el pago", exception);
        }
    }

    @Override
    public Optional<PagoExterno> buscarPagoPorExternalReference(String externalReference) {
        exigirConfiguracion();
        if (externalReference == null || externalReference.isBlank()) return Optional.empty();
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/payments/search")
                            .queryParam("external_reference", externalReference)
                            .queryParam("sort", "date_created")
                            .queryParam("criteria", "desc")
                            .build())
                    .header("Authorization", "Bearer " + properties.getAccessToken().trim())
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.path("results").isArray()) return Optional.empty();
            List<PagoExterno> encontrados = new ArrayList<>();
            for (JsonNode node : response.path("results")) {
                PagoExterno pago = mapearPago(node);
                if (externalReference.equals(pago.externalReference())) encontrados.add(pago);
            }
            return seleccionarPago(encontrados);
        } catch (RestClientResponseException exception) {
            throw errorApi("buscar el pago", exception);
        } catch (RestClientException exception) {
            throw errorConexion("buscar el pago", exception);
        }
    }

    static Optional<PagoExterno> seleccionarPago(List<PagoExterno> pagosOrdenadosDesc) {
        if (pagosOrdenadosDesc == null || pagosOrdenadosDesc.isEmpty()) return Optional.empty();
        // No ocultar dinero cobrado si un reintento posterior fue rechazado.
        // Un refund/chargeback actualiza el mismo payment, por lo que ya no
        // aparecera como approved y entonces prevalece el estado mas reciente.
        return pagosOrdenadosDesc.stream()
                .filter(pago -> "approved".equalsIgnoreCase(pago.estado()))
                .findFirst()
                .or(() -> pagosOrdenadosDesc.stream().findFirst());
    }

    private PagoExterno mapearPago(JsonNode node) {
        return new PagoExterno(
                texto(node, "id"), texto(node, "status"), texto(node, "external_reference"),
                decimal(node, "transaction_amount"), texto(node, "currency_id"),
                fecha(node.path("date_approved").asText(null)), texto(node, "status_detail"));
    }

    private void agregarUrlsPublicas(Map<String, Object> body, SolicitudPreferencia solicitud) {
        if (esUrlPublicaHttps(properties.getReturnUrl())) {
            Map<String, String> urls = new LinkedHashMap<>();
            urls.put("success", retorno(solicitud, "success"));
            urls.put("pending", retorno(solicitud, "pending"));
            urls.put("failure", retorno(solicitud, "failure"));
            body.put("back_urls", urls);
            body.put("auto_return", "approved");
        }
        if (esUrlPublicaHttps(properties.getNotificationUrl()) && properties.tieneWebhookSecret()) {
            body.put("notification_url", properties.getNotificationUrl().trim());
        }
    }

    private String retorno(SolicitudPreferencia solicitud, String resultado) {
        String base = properties.getReturnUrl().trim();
        String separador = base.contains("?") ? "&" : "?";
        String encuentro = solicitud.encuentroId() == null
                ? ""
                : "&encuentroId=" + solicitud.encuentroId();
        return base + separador + "pagoId=" + solicitud.pagoId()
                + "&solicitudId=" + solicitud.solicitudId()
                + encuentro
                + "&resultado=" + resultado;
    }

    static boolean esUrlPublicaHttps(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            URI uri = URI.create(value.trim());
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && !"localhost".equalsIgnoreCase(host) && !host.startsWith("127.");
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void exigirConfiguracion() {
        if (!properties.tieneAccessToken()) {
            throw new IllegalStateException("Mercado Pago no esta configurado. Defini SERVIFY_MERCADOPAGO_ACCESS_TOKEN en el backend");
        }
    }

    private IllegalStateException errorApi(String accion, RestClientResponseException exception) {
        String detalle = exception.getResponseBodyAsString();
        detalle = detalle == null ? "" : detalle.replaceAll("[\\r\\n]+", " ").trim();
        if (detalle.length() > 240) detalle = detalle.substring(0, 240);
        return new IllegalStateException("No se pudo " + accion + " en Mercado Pago (HTTP "
                + exception.getStatusCode().value() + ")" + (detalle.isBlank() ? "" : ": " + detalle));
    }

    private IllegalStateException errorConexion(String accion, RestClientException exception) {
        return new IllegalStateException("No se pudo " + accion
                + " en Mercado Pago por un problema de conexion. Reintenta sin crear otro pago", exception);
    }

    private static String texto(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static String primerTexto(JsonNode node, String primero, String segundo) {
        String value = texto(node, primero);
        return value == null || value.isBlank() ? texto(node, segundo) : value;
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNumber()) return value.decimalValue();
        if (value.isTextual()) {
            try { return new BigDecimal(value.asText()); } catch (NumberFormatException ignored) { return null; }
        }
        return null;
    }

    private static LocalDateTime fecha(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (RuntimeException ignored) {
            try { return LocalDateTime.parse(value); } catch (RuntimeException alsoIgnored) { return null; }
        }
    }
}
