package com.servify.pagos.infrastructure.mercadopago;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "servify.mercadopago")
public class MercadoPagoProperties {
    private String apiBaseUrl = "https://api.mercadopago.com";
    private String accessToken = "";
    private String returnUrl = "";
    private String notificationUrl = "";
    private String webhookSecret = "";
    private boolean required = true;

    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getNotificationUrl() { return notificationUrl; }
    public void setNotificationUrl(String notificationUrl) { this.notificationUrl = notificationUrl; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public boolean tieneAccessToken() { return accessToken != null && !accessToken.isBlank(); }
    public boolean tieneWebhookSecret() { return webhookSecret != null && !webhookSecret.isBlank(); }
}
