package com.servify.pagos.infrastructure.web;

import com.servify.administracion.infrastructure.web.AdminAuthorizationService;
import com.servify.pagos.application.dto.IniciarPagoServicioCommand;
import com.servify.pagos.application.dto.PagoServicioResult;
import com.servify.pagos.application.dto.SincronizarPagoServicioCommand;
import com.servify.pagos.application.port.in.ConsultarPagoServicioUseCase;
import com.servify.pagos.application.port.in.IniciarPagoServicioUseCase;
import com.servify.pagos.application.port.in.SincronizarPagoServicioUseCase;
import com.servify.pagos.infrastructure.mercadopago.MercadoPagoWebhookSignatureValidator;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PagosApiController {
    private final IniciarPagoServicioUseCase iniciarPago;
    private final SincronizarPagoServicioUseCase sincronizarPago;
    private final ConsultarPagoServicioUseCase consultarPago;
    private final MercadoPagoWebhookSignatureValidator signatureValidator;
    private final AdminAuthorizationService authorizationService;

    public PagosApiController(IniciarPagoServicioUseCase iniciarPago,
                              SincronizarPagoServicioUseCase sincronizarPago,
                              ConsultarPagoServicioUseCase consultarPago,
                              MercadoPagoWebhookSignatureValidator signatureValidator,
                              AdminAuthorizationService authorizationService) {
        this.iniciarPago = iniciarPago;
        this.sincronizarPago = sincronizarPago;
        this.consultarPago = consultarPago;
        this.signatureValidator = signatureValidator;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/solicitudes/{solicitudId}/pagos/checkout")
    public ResponseEntity<PagoServicioResult> iniciarCheckout(
            @PathVariable UUID solicitudId,
            @RequestBody IniciarCheckoutRequest request,
            HttpServletRequest httpRequest) {
        Usuario autenticado = authorizationService.requireActiveUser(httpRequest);
        exigirMismoUsuario(autenticado, request.solicitanteId);
        return ResponseEntity.ok(iniciarPago.iniciar(new IniciarPagoServicioCommand(
                solicitudId, request.solicitanteId, request.asignacionServicioId, request.encuentroId)));
    }

    @PostMapping("/pagos/{pagoId}/sincronizacion")
    public ResponseEntity<PagoServicioResult> sincronizar(
            @PathVariable UUID pagoId,
            @RequestBody SincronizarPagoRequest request,
            HttpServletRequest httpRequest) {
        Usuario autenticado = authorizationService.requireActiveUser(httpRequest);
        exigirMismoUsuario(autenticado, request.solicitanteId);
        return ResponseEntity.ok(sincronizarPago.sincronizar(new SincronizarPagoServicioCommand(
                pagoId, request.solicitanteId, request.mercadoPagoPaymentId)));
    }

    @GetMapping("/solicitudes/{solicitudId}/pagos/estado")
    public ResponseEntity<PagoServicioResult> obtenerEstado(
            @PathVariable UUID solicitudId,
            @RequestParam UUID asignacionServicioId,
            @RequestParam(required = false) UUID encuentroId,
            HttpServletRequest httpRequest) {
        Usuario autenticado = authorizationService.requireActiveUser(httpRequest);
        return consultarPago.obtenerEstado(solicitudId, asignacionServicioId, encuentroId, autenticado.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/pagos/mercadopago/webhook")
    public ResponseEntity<Void> webhook(
            @RequestParam(required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataIdQuery,
            @RequestParam(name = "data_id", required = false) String dataIdLegacy,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) Map<String, Object> body) {
        String tipo = type != null ? type : texto(body, "type");
        if (tipo != null && !"payment".equalsIgnoreCase(tipo)) {
            return ResponseEntity.ok().build();
        }
        String dataId = dataIdQuery != null ? dataIdQuery
                : dataIdLegacy != null ? dataIdLegacy : dataId(body);
        signatureValidator.validar(xSignature, xRequestId, dataId);
        sincronizarPago.sincronizarWebhook(dataId);
        return ResponseEntity.ok().build();
    }

    private static String dataId(Map<String, Object> body) {
        if (body == null) return null;
        Object data = body.get("data");
        if (data instanceof Map<?, ?> map && map.get("id") != null) return String.valueOf(map.get("id"));
        return null;
    }

    private static String texto(Map<String, Object> body, String key) {
        return body == null || body.get(key) == null ? null : String.valueOf(body.get(key));
    }

    private static void exigirMismoUsuario(Usuario autenticado, UUID declarado) {
        if (autenticado == null || declarado == null || !autenticado.getId().equals(declarado)) {
            throw new ForbiddenException("La identidad autenticada no coincide con el solicitante del pago");
        }
    }

    public static class IniciarCheckoutRequest {
        public UUID solicitanteId;
        public UUID asignacionServicioId;
        public UUID encuentroId;
    }

    public static class SincronizarPagoRequest {
        public UUID solicitanteId;
        public String mercadoPagoPaymentId;
    }
}
