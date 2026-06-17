package com.servify.administracion.infrastructure.web;

import com.servify.administracion.application.dto.ConfiguracionGeneralResult;
import com.servify.administracion.application.dto.AplicarMedidaAdministrativaUsuarioCommand;
import com.servify.administracion.application.dto.MedidaAdministrativaUsuarioResult;
import com.servify.administracion.application.dto.ModerarPublicacionCommand;
import com.servify.administracion.application.port.in.AplicarMedidaAdministrativaUsuarioUseCase;
import com.servify.administracion.application.port.in.ListarPublicacionesAdminUseCase;
import com.servify.administracion.application.port.in.ModerarPublicacionUseCase;
import com.servify.administracion.application.port.in.ObtenerMedidasAdministrativasDeUsuarioUseCase;
import com.servify.administracion.application.port.in.ObtenerConfiguracionGeneralUseCase;
import com.servify.administracion.domain.enumtype.TipoMedida;
import com.servify.notificaciones.application.dto.CrearNotificacionUsuarioCommand;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.CambiarEstadoCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.CrearCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.CambiarEstadoCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.CrearCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.ListarCategoriasUseCase;
import com.servify.publicaciones.application.port.in.ObtenerPublicacionUseCase;
import com.servify.publicaciones.domain.enumtype.EstadoCategoria;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.shared.domain.exception.ValidationException;
import com.servify.usuarios.application.dto.CambiarEstadoUsuarioCommand;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.CambiarEstadoUsuarioUseCase;
import com.servify.usuarios.application.port.in.ListarUsuariosUseCase;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final AdminAuthorizationService adminAuthorizationService;
    private final ModerarPublicacionUseCase moderarPublicacionUseCase;
    private final ListarPublicacionesAdminUseCase listarPublicacionesAdminUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;
    private final AplicarMedidaAdministrativaUsuarioUseCase aplicarMedidaAdministrativaUsuarioUseCase;
    private final ObtenerMedidasAdministrativasDeUsuarioUseCase obtenerMedidasAdministrativasDeUsuarioUseCase;
    private final ObtenerConfiguracionGeneralUseCase obtenerConfiguracionGeneralUseCase;
    private final ObtenerPublicacionUseCase obtenerPublicacionUseCase;
    private final CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase;
    private final ListarCategoriasUseCase listarCategoriasUseCase;
    private final CrearCategoriaServicioUseCase crearCategoriaServicioUseCase;
    private final CambiarEstadoCategoriaServicioUseCase cambiarEstadoCategoriaServicioUseCase;

    public AdminApiController(
            AdminAuthorizationService adminAuthorizationService,
            ModerarPublicacionUseCase moderarPublicacionUseCase,
            ListarPublicacionesAdminUseCase listarPublicacionesAdminUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase,
            CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase,
            AplicarMedidaAdministrativaUsuarioUseCase aplicarMedidaAdministrativaUsuarioUseCase,
            ObtenerMedidasAdministrativasDeUsuarioUseCase obtenerMedidasAdministrativasDeUsuarioUseCase,
            ObtenerConfiguracionGeneralUseCase obtenerConfiguracionGeneralUseCase,
            ObtenerPublicacionUseCase obtenerPublicacionUseCase,
            CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase,
            ListarCategoriasUseCase listarCategoriasUseCase,
            CrearCategoriaServicioUseCase crearCategoriaServicioUseCase,
            CambiarEstadoCategoriaServicioUseCase cambiarEstadoCategoriaServicioUseCase
    ) {
        this.adminAuthorizationService = adminAuthorizationService;
        this.moderarPublicacionUseCase = moderarPublicacionUseCase;
        this.listarPublicacionesAdminUseCase = listarPublicacionesAdminUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.cambiarEstadoUsuarioUseCase = cambiarEstadoUsuarioUseCase;
        this.aplicarMedidaAdministrativaUsuarioUseCase = aplicarMedidaAdministrativaUsuarioUseCase;
        this.obtenerMedidasAdministrativasDeUsuarioUseCase = obtenerMedidasAdministrativasDeUsuarioUseCase;
        this.obtenerConfiguracionGeneralUseCase = obtenerConfiguracionGeneralUseCase;
        this.obtenerPublicacionUseCase = obtenerPublicacionUseCase;
        this.crearNotificacionUsuarioUseCase = crearNotificacionUsuarioUseCase;
        this.listarCategoriasUseCase = listarCategoriasUseCase;
        this.crearCategoriaServicioUseCase = crearCategoriaServicioUseCase;
        this.cambiarEstadoCategoriaServicioUseCase = cambiarEstadoCategoriaServicioUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResult> obtenerAdminActual(HttpServletRequest request) {
        Usuario admin = adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(toResult(admin));
    }

    @GetMapping("/publicaciones")
    public ResponseEntity<List<PublicacionServicioResult>> listarPublicaciones(
            @RequestParam(required = false) EstadoPublicacion estado,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(listarPublicacionesAdminUseCase.listarPorEstado(estado));
    }

    @PatchMapping("/publicaciones/{publicacionId}/moderacion")
    public ResponseEntity<Void> moderarPublicacion(
            @PathVariable UUID publicacionId,
            @RequestBody ModerarPublicacionRequest body,
            HttpServletRequest request
    ) {
        Usuario admin = adminAuthorizationService.requireAdmin(request);
        if (body == null || body.estadoDestino == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
        PublicacionServicioResult publicacion = obtenerPublicacionUseCase.obtenerPorId(publicacionId)
                .orElseThrow(() -> new ValidationException("La publicacion no existe"));
        moderarPublicacionUseCase.moderar(
                new ModerarPublicacionCommand(
                        publicacionId,
                        admin.getId(),
                        body.estadoDestino,
                        body.motivo
                )
        );
        notificarCambioPublicacion(publicacion, body.estadoDestino, body.motivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResult>> listarUsuarios(
            @RequestParam(defaultValue = "ACTIVO") EstadoUsuario estado,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(listarUsuariosUseCase.listarPorEstado(estado));
    }

    @PatchMapping("/usuarios/{usuarioId}/estado")
    public ResponseEntity<Void> cambiarEstadoUsuario(
            @PathVariable UUID usuarioId,
            @RequestBody CambiarEstadoUsuarioAdminRequest body,
            HttpServletRequest request
    ) {
        Usuario admin = adminAuthorizationService.requireAdmin(request);
        if (body == null || body.nuevoEstado == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
        if (admin.getId().equals(usuarioId) && body.nuevoEstado != EstadoUsuario.ACTIVO) {
            throw new ValidationException("No podes aplicar una medida restrictiva sobre tu propia cuenta administradora");
        }

        EstadoUsuario nuevoEstado = body.nuevoEstado;
        if (nuevoEstado == EstadoUsuario.BLOQUEADO || nuevoEstado == EstadoUsuario.SUSPENDIDO) {
            aplicarMedidaAdministrativaUsuarioUseCase.aplicar(
                    new AplicarMedidaAdministrativaUsuarioCommand(
                            usuarioId,
                            admin.getId(),
                            nuevoEstado == EstadoUsuario.BLOQUEADO ? TipoMedida.BLOQUEO : TipoMedida.SUSPENSION,
                            body.motivo,
                            body.fechaFinVigencia
                    )
            );
        } else {
            cambiarEstadoUsuarioUseCase.cambiarEstado(new CambiarEstadoUsuarioCommand(usuarioId, nuevoEstado));
        }

        notificarCambioUsuario(usuarioId, nuevoEstado, body.motivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/{usuarioId}/medidas")
    public ResponseEntity<List<MedidaAdministrativaUsuarioResult>> obtenerMedidasUsuario(
            @PathVariable UUID usuarioId,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(obtenerMedidasAdministrativasDeUsuarioUseCase.obtenerPorUsuarioId(usuarioId));
    }

    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionGeneralResult> obtenerConfiguracion(HttpServletRequest request) {
        adminAuthorizationService.requireAdmin(request);
        return obtenerConfiguracionGeneralUseCase.obtenerVigente()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaServicioResult>> listarCategorias(HttpServletRequest request) {
        adminAuthorizationService.requireAdmin(request);
        return ResponseEntity.ok(listarCategoriasUseCase.listarTodas());
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaServicioResult> crearCategoria(
            @RequestBody CrearCategoriaAdminRequest body,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        if (body == null || body.nombre == null || body.nombre.isBlank()) {
            throw new ValidationException("El nombre de la categoria es obligatorio");
        }
        CategoriaServicioResult result = crearCategoriaServicioUseCase.crear(
                new CrearCategoriaServicioCommand(body.nombre.trim(), body.descripcion)
        );
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/categorias/{categoriaId}/estado")
    public ResponseEntity<CategoriaServicioResult> cambiarEstadoCategoria(
            @PathVariable UUID categoriaId,
            @RequestBody CambiarEstadoCategoriaAdminRequest body,
            HttpServletRequest request
    ) {
        adminAuthorizationService.requireAdmin(request);
        if (body == null || body.estadoDestino == null) {
            throw new ValidationException("El estado destino es obligatorio");
        }
        CategoriaServicioResult result = cambiarEstadoCategoriaServicioUseCase.cambiarEstado(
                new CambiarEstadoCategoriaServicioCommand(categoriaId, body.estadoDestino, body.motivo)
        );
        return ResponseEntity.ok(result);
    }

    public static class ModerarPublicacionRequest {
        public String estadoDestino;
        public String motivo;
    }

    public static class CrearCategoriaAdminRequest {
        public String nombre;
        public String descripcion;
    }

    public static class CambiarEstadoCategoriaAdminRequest {
        public EstadoCategoria estadoDestino;
        public String motivo;
    }

    public static class CambiarEstadoUsuarioAdminRequest {
        public EstadoUsuario nuevoEstado;
        public String motivo;
        public LocalDateTime fechaFinVigencia;
    }

    private UsuarioResult toResult(Usuario usuario) {
        return new UsuarioResult(
                usuario.getId(),
                usuario.getContacto() != null ? usuario.getContacto().getEmail() : null,
                usuario.getNombreUsuario(),
                usuario.getContacto() != null ? usuario.getContacto().getTelefono() : null,
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getEstadoValidacionIdentidad(),
                usuario.getFechaRegistro()
        );
    }

    private void notificarCambioPublicacion(
            PublicacionServicioResult publicacion,
            String estadoDestino,
            String motivo
    ) {
        if (publicacion == null || publicacion.getUsuarioId() == null) {
            return;
        }
        String tituloPublicacion = publicacion.getTitulo() != null ? publicacion.getTitulo() : "tu publicacion";
        crearNotificacionUsuarioUseCase.crear(new CrearNotificacionUsuarioCommand(
                publicacion.getUsuarioId(),
                TipoNotificacion.MODERACION_PUBLICACION,
                "Cambio en tu publicacion",
                "Servify cambio el estado de \"" + tituloPublicacion + "\" a " + estadoDestino
                        + motivoSuffix(motivo) + ".",
                "PUBLICACION",
                publicacion.getId()
        ));
    }

    private void notificarCambioUsuario(UUID usuarioId, EstadoUsuario nuevoEstado, String motivo) {
        crearNotificacionUsuarioUseCase.crear(new CrearNotificacionUsuarioCommand(
                usuarioId,
                TipoNotificacion.MODERACION_USUARIO,
                "Cambio en tu cuenta",
                "Servify cambio el estado de tu cuenta a " + nuevoEstado.name()
                        + motivoSuffix(motivo) + ".",
                "USUARIO",
                usuarioId
        ));
    }

    private String motivoSuffix(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return "";
        }
        return ". Motivo: " + motivo.trim();
    }
}
