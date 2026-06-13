package com.servify.notificaciones.infrastructure.web;

import com.servify.administracion.infrastructure.web.AdminAuthorizationService;
import com.servify.notificaciones.application.dto.NotificacionUsuarioResult;
import com.servify.notificaciones.application.port.in.ListarNotificacionesUsuarioUseCase;
import com.servify.notificaciones.application.port.in.MarcarNotificacionLeidaUseCase;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/notificaciones")
public class NotificacionesApiController {

    private final ListarNotificacionesUsuarioUseCase listarNotificacionesUsuarioUseCase;
    private final MarcarNotificacionLeidaUseCase marcarNotificacionLeidaUseCase;
    private final AdminAuthorizationService adminAuthorizationService;

    public NotificacionesApiController(
            ListarNotificacionesUsuarioUseCase listarNotificacionesUsuarioUseCase,
            MarcarNotificacionLeidaUseCase marcarNotificacionLeidaUseCase,
            AdminAuthorizationService adminAuthorizationService
    ) {
        this.listarNotificacionesUsuarioUseCase = listarNotificacionesUsuarioUseCase;
        this.marcarNotificacionLeidaUseCase = marcarNotificacionLeidaUseCase;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionUsuarioResult>> listar(
            @PathVariable UUID usuarioId,
            HttpServletRequest request
    ) {
        requireSameUser(usuarioId, request);
        return ResponseEntity.ok(listarNotificacionesUsuarioUseCase.listarPorUsuarioId(usuarioId));
    }

    @PatchMapping("/{notificacionId}/lectura")
    public ResponseEntity<NotificacionUsuarioResult> marcarLeida(
            @PathVariable UUID usuarioId,
            @PathVariable UUID notificacionId,
            HttpServletRequest request
    ) {
        requireSameUser(usuarioId, request);
        return ResponseEntity.ok(marcarNotificacionLeidaUseCase.marcarLeida(usuarioId, notificacionId));
    }

    private void requireSameUser(UUID usuarioId, HttpServletRequest request) {
        Usuario autenticado = adminAuthorizationService.requireAuthenticatedUser(request);
        if (!usuarioId.equals(autenticado.getId())) {
            throw new ForbiddenException("No podes consultar notificaciones de otro usuario");
        }
    }
}
