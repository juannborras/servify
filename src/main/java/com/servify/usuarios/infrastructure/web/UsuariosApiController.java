package com.servify.usuarios.infrastructure.web;

import com.servify.administracion.infrastructure.web.AdminAuthorizationService;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.shared.infrastructure.web.MvpWebMapper;
import com.servify.usuarios.application.dto.ActualizarCuentaUsuarioCommand;
import com.servify.usuarios.application.dto.ActualizarPerfilUsuarioCommand;
import com.servify.usuarios.application.dto.CambiarEstadoUsuarioCommand;
import com.servify.usuarios.application.dto.ConfiguracionCuentaResult;
import com.servify.usuarios.application.dto.CrearUsuarioCommand;
import com.servify.usuarios.application.dto.PerfilUsuarioResult;
import com.servify.usuarios.application.dto.ReputacionUsuarioResult;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.ActualizarCuentaUsuarioUseCase;
import com.servify.usuarios.application.port.in.ActualizarPerfilUsuarioUseCase;
import com.servify.usuarios.application.port.in.CambiarEstadoUsuarioUseCase;
import com.servify.usuarios.application.port.in.CrearUsuarioUseCase;
import com.servify.usuarios.application.port.in.ListarUsuariosUseCase;
import com.servify.usuarios.application.port.in.ObtenerConfiguracionCuentaUseCase;
import com.servify.usuarios.application.port.in.ObtenerPerfilUsuarioUseCase;
import com.servify.usuarios.application.port.in.ObtenerReputacionUsuarioUseCase;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.Rol;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuariosApiController {

    private final CrearUsuarioUseCase crearUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final ActualizarCuentaUsuarioUseCase actualizarCuentaUsuarioUseCase;
    private final ActualizarPerfilUsuarioUseCase actualizarPerfilUsuarioUseCase;
    private final AdminAuthorizationService adminAuthorizationService;
    private final ObtenerPerfilUsuarioUseCase obtenerPerfilUsuarioUseCase;
    private final ObtenerConfiguracionCuentaUseCase obtenerConfiguracionCuentaUseCase;
    private final ObtenerReputacionUsuarioUseCase obtenerReputacionUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    public UsuariosApiController(
            CrearUsuarioUseCase crearUsuarioUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase,
            ActualizarCuentaUsuarioUseCase actualizarCuentaUsuarioUseCase,
            ActualizarPerfilUsuarioUseCase actualizarPerfilUsuarioUseCase,
            AdminAuthorizationService adminAuthorizationService,
            ObtenerPerfilUsuarioUseCase obtenerPerfilUsuarioUseCase,
            ObtenerConfiguracionCuentaUseCase obtenerConfiguracionCuentaUseCase,
            ObtenerReputacionUsuarioUseCase obtenerReputacionUsuarioUseCase,
            CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase
    ) {
        this.crearUsuarioUseCase = crearUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.actualizarCuentaUsuarioUseCase = actualizarCuentaUsuarioUseCase;
        this.actualizarPerfilUsuarioUseCase = actualizarPerfilUsuarioUseCase;
        this.adminAuthorizationService = adminAuthorizationService;
        this.obtenerPerfilUsuarioUseCase = obtenerPerfilUsuarioUseCase;
        this.obtenerConfiguracionCuentaUseCase = obtenerConfiguracionCuentaUseCase;
        this.obtenerReputacionUsuarioUseCase = obtenerReputacionUsuarioUseCase;
        this.cambiarEstadoUsuarioUseCase = cambiarEstadoUsuarioUseCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioResult> crearUsuario(@RequestBody CrearUsuarioRequest request) {
        UsuarioResult result = crearUsuarioUseCase.crear(
                new CrearUsuarioCommand(request.email, request.nombreUsuario, request.telefono, request.rol)
        );
        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + result.getId()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResult>> listarUsuarios(
            @RequestParam(defaultValue = "ACTIVO") EstadoUsuario estado,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(listarUsuariosUseCase.listarPorEstado(estado));
    }

    @PutMapping("/{usuarioId}/perfil")
    public ResponseEntity<PerfilUsuarioResult> actualizarPerfil(
            @PathVariable UUID usuarioId,
            @RequestBody ActualizarPerfilRequest request
    ) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacion);
        PerfilUsuarioResult result = actualizarPerfilUsuarioUseCase.actualizar(
                new ActualizarPerfilUsuarioCommand(
                        usuarioId,
                        request.nombre,
                        request.apellido,
                        request.edad,
                        request.fotoPerfilUrl,
                        ubicacion,
                        request.descripcionPersonal
                )
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{usuarioId}/perfil")
    public ResponseEntity<PerfilUsuarioResult> obtenerPerfil(@PathVariable UUID usuarioId) {
        return obtenerPerfilUsuarioUseCase.obtenerPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{usuarioId}/cuenta")
    public ResponseEntity<ConfiguracionCuentaResult> obtenerConfiguracionCuenta(@PathVariable UUID usuarioId) {
        return obtenerConfiguracionCuentaUseCase.obtenerPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{usuarioId}/reputacion")
    public ResponseEntity<ReputacionUsuarioResult> obtenerReputacion(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(obtenerReputacionUsuarioUseCase.obtenerPorUsuarioId(usuarioId));
    }

    @PatchMapping("/{usuarioId}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable UUID usuarioId,
            @RequestBody CambiarEstadoUsuarioRequest body,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        cambiarEstadoUsuarioUseCase.cambiarEstado(
                new CambiarEstadoUsuarioCommand(usuarioId, body.nuevoEstado)
        );
        return ResponseEntity.noContent().build();
    }

    public static class CrearUsuarioRequest {
        public String email;
        public String nombreUsuario;
        public String telefono;
        public Rol rol;
    }

    @PatchMapping("/{usuarioId}/cuenta")
    public ResponseEntity<UsuarioResult> actualizarCuenta(
            @PathVariable UUID usuarioId,
            @RequestBody ActualizarCuentaRequest request
    ) {
        UsuarioResult result = actualizarCuentaUsuarioUseCase.actualizar(
                new ActualizarCuentaUsuarioCommand(usuarioId, request.nombreUsuario)
        );
        return ResponseEntity.ok(result);
    }

    public static class ActualizarPerfilRequest {
        public String nombre;
        public String apellido;
        public Integer edad;
        public String fotoPerfilUrl;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public String descripcionPersonal;
    }

    public static class ActualizarCuentaRequest {
        public String nombreUsuario;
    }

    public static class CambiarEstadoUsuarioRequest {
        public EstadoUsuario nuevoEstado;
    }
}
