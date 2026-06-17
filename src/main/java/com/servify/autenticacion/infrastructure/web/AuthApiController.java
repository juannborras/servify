package com.servify.autenticacion.infrastructure.web;

import com.servify.autenticacion.application.dto.AutenticarConIdentidadExternaCommand;
import com.servify.autenticacion.application.dto.CerrarSesionCommand;
import com.servify.autenticacion.application.dto.IniciarSesionCommand;
import com.servify.autenticacion.application.dto.RecuperacionPasswordResult;
import com.servify.autenticacion.application.dto.RegistrarCredencialesCommand;
import com.servify.autenticacion.application.dto.RestablecerPasswordCommand;
import com.servify.autenticacion.application.dto.RenovarTokenCommand;
import com.servify.autenticacion.application.dto.SesionResult;
import com.servify.autenticacion.application.dto.SolicitarRecuperacionPasswordCommand;
import com.servify.autenticacion.application.port.in.AutenticarConIdentidadExternaUseCase;
import com.servify.autenticacion.application.port.in.CerrarSesionUseCase;
import com.servify.autenticacion.application.port.in.IniciarSesionUseCase;
import com.servify.autenticacion.application.port.in.RegistrarCredencialesUseCase;
import com.servify.autenticacion.application.port.in.RestablecerPasswordUseCase;
import com.servify.autenticacion.application.port.in.RenovarTokenUseCase;
import com.servify.autenticacion.application.port.in.SolicitarRecuperacionPasswordUseCase;
import com.servify.autenticacion.domain.enumtype.ProveedorIdentidadExterna;
import com.servify.usuarios.domain.enumtype.Rol;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final RegistrarCredencialesUseCase registrarCredencialesUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;
    private final RenovarTokenUseCase renovarTokenUseCase;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final AutenticarConIdentidadExternaUseCase autenticarConIdentidadExternaUseCase;
    private final SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase;
    private final RestablecerPasswordUseCase restablecerPasswordUseCase;

    public AuthApiController(
            RegistrarCredencialesUseCase registrarCredencialesUseCase,
            IniciarSesionUseCase iniciarSesionUseCase,
            RenovarTokenUseCase renovarTokenUseCase,
            CerrarSesionUseCase cerrarSesionUseCase,
            AutenticarConIdentidadExternaUseCase autenticarConIdentidadExternaUseCase,
            SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase,
            RestablecerPasswordUseCase restablecerPasswordUseCase
    ) {
        this.registrarCredencialesUseCase = registrarCredencialesUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        this.renovarTokenUseCase = renovarTokenUseCase;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        this.autenticarConIdentidadExternaUseCase = autenticarConIdentidadExternaUseCase;
        this.solicitarRecuperacionPasswordUseCase = solicitarRecuperacionPasswordUseCase;
        this.restablecerPasswordUseCase = restablecerPasswordUseCase;
    }

    @PostMapping("/credenciales")
    public ResponseEntity<Void> registrarCredenciales(@RequestBody RegistrarCredencialesRequest request) {
        registrarCredencialesUseCase.registrar(
                new RegistrarCredencialesCommand(
                        request.usuarioId,
                        request.emailAcceso,
                        request.passwordPlano
                )
        );
        return ResponseEntity
                .created(URI.create("/api/v1/auth/credenciales/" + request.usuarioId))
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<SesionResult> iniciarSesion(@RequestBody IniciarSesionRequest request) {
        SesionResult result = iniciarSesionUseCase.iniciar(
                new IniciarSesionCommand(request.emailAcceso, request.passwordPlano)
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset")
    public ResponseEntity<RecuperacionPasswordResult> solicitarRecuperacionPassword(
            @RequestBody SolicitarRecuperacionPasswordRequest request
    ) {
        RecuperacionPasswordResult result = solicitarRecuperacionPasswordUseCase.solicitar(
                new SolicitarRecuperacionPasswordCommand(request.email)
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> restablecerPassword(@RequestBody RestablecerPasswordRequest request) {
        restablecerPasswordUseCase.restablecer(
                new RestablecerPasswordCommand(request.token, request.nuevaPassword)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/social/{proveedor}")
    public ResponseEntity<SesionResult> autenticarConIdentidadExterna(
            @PathVariable String proveedor,
            @RequestBody AutenticacionExternaRequest request
    ) {
        ProveedorIdentidadExterna proveedorIdentidad = ProveedorIdentidadExterna.desdeApiValue(proveedor);
        AutenticacionExternaRequest body = request != null ? request : new AutenticacionExternaRequest();
        SesionResult result = autenticarConIdentidadExternaUseCase.autenticar(
                new AutenticarConIdentidadExternaCommand(
                        proveedorIdentidad,
                        body.idToken,
                        body.nonce,
                        rolDesdeRequest(body.rol),
                        body.telefono
                )
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SesionResult> renovarToken(@RequestBody RenovarTokenRequest request) {
        SesionResult result = renovarTokenUseCase.renovar(new RenovarTokenCommand(request.refreshToken));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion(@RequestBody CerrarSesionRequest request) {
        cerrarSesionUseCase.cerrar(new CerrarSesionCommand(request.usuarioId, request.refreshToken));
        return ResponseEntity.noContent().build();
    }

    public static class RegistrarCredencialesRequest {
        public UUID usuarioId;
        public String emailAcceso;
        public String passwordPlano;
    }

    public static class IniciarSesionRequest {
        public String emailAcceso;
        public String passwordPlano;
    }

    public static class SolicitarRecuperacionPasswordRequest {
        public String email;
    }

    public static class RestablecerPasswordRequest {
        public String token;
        public String nuevaPassword;
    }

    public static class RenovarTokenRequest {
        public String refreshToken;
    }

    public static class CerrarSesionRequest {
        public UUID usuarioId;
        public String refreshToken;
    }

    public static class AutenticacionExternaRequest {
        public String idToken;
        public String nonce;
        public String rol;
        public String telefono;
    }

    private Rol rolDesdeRequest(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }
        return Rol.valueOf(rol.trim().toUpperCase(Locale.ROOT));
    }
}
