package com.servify.chat.infrastructure.web;

import com.servify.administracion.infrastructure.web.AdminAuthorizationService;
import com.servify.chat.application.dto.EnviarMensajeChatCommand;
import com.servify.chat.application.dto.MensajeChatResult;
import com.servify.chat.application.port.in.EnviarMensajeChatUseCase;
import com.servify.chat.application.port.in.ListarMensajesChatUseCase;
import com.servify.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solicitudes/{solicitudId}/chat")
public class ChatSolicitudesApiController {

    private final ListarMensajesChatUseCase listarMensajesChatUseCase;
    private final EnviarMensajeChatUseCase enviarMensajeChatUseCase;
    private final AdminAuthorizationService adminAuthorizationService;

    public ChatSolicitudesApiController(
            ListarMensajesChatUseCase listarMensajesChatUseCase,
            EnviarMensajeChatUseCase enviarMensajeChatUseCase,
            AdminAuthorizationService adminAuthorizationService
    ) {
        this.listarMensajesChatUseCase = listarMensajesChatUseCase;
        this.enviarMensajeChatUseCase = enviarMensajeChatUseCase;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeChatResult>> listar(
            @PathVariable UUID solicitudId,
            @RequestParam UUID prestadorId,
            HttpServletRequest request
    ) {
        Usuario usuario = adminAuthorizationService.requireAuthenticatedUser(request);
        return ResponseEntity.ok(listarMensajesChatUseCase.listar(solicitudId, prestadorId, usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<MensajeChatResult> enviar(
            @PathVariable UUID solicitudId,
            @RequestBody EnviarMensajeChatRequest body,
            HttpServletRequest request
    ) {
        Usuario usuario = adminAuthorizationService.requireAuthenticatedUser(request);
        MensajeChatResult result = enviarMensajeChatUseCase.enviar(new EnviarMensajeChatCommand(
                solicitudId,
                body.prestadorId,
                usuario.getId(),
                body.contenido
        ));
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + solicitudId + "/chat/" + result.getId()))
                .body(result);
    }

    public static class EnviarMensajeChatRequest {
        public UUID prestadorId;
        public String contenido;
    }
}
