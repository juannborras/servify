package com.servify.publicaciones.infrastructure.web;

import com.servify.publicaciones.application.dto.BuscarPublicacionesCompatiblesQuery;
import com.servify.publicaciones.application.dto.CambiarEstadoCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.CambiarEstadoPublicacionCommand;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.ActualizarPublicacionCommand;
import com.servify.publicaciones.application.dto.CrearCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.CrearPublicacionCommand;
import com.servify.publicaciones.application.dto.PublicacionCompatibleResult;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.in.BuscarPublicacionesCompatiblesUseCase;
import com.servify.publicaciones.application.port.in.CambiarEstadoCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.CambiarEstadoPublicacionUseCase;
import com.servify.publicaciones.application.port.in.ActualizarPublicacionUseCase;
import com.servify.publicaciones.application.port.in.CrearCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.CrearPublicacionUseCase;
import com.servify.publicaciones.application.port.in.ListarCategoriasActivasUseCase;
import com.servify.publicaciones.application.port.in.ListarPublicacionesPorCategoriaUseCase;
import com.servify.publicaciones.application.port.in.ListarPublicacionesDeUsuarioUseCase;
import com.servify.publicaciones.application.port.in.ObtenerPublicacionUseCase;
import com.servify.publicaciones.domain.enumtype.EstadoCategoria;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.shared.infrastructure.web.MvpWebMapper;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PublicacionesApiController {

    private final CrearCategoriaServicioUseCase crearCategoriaServicioUseCase;
    private final CambiarEstadoCategoriaServicioUseCase cambiarEstadoCategoriaServicioUseCase;
    private final ListarCategoriasActivasUseCase listarCategoriasActivasUseCase;
    private final CrearPublicacionUseCase crearPublicacionUseCase;
    private final ActualizarPublicacionUseCase actualizarPublicacionUseCase;
    private final CambiarEstadoPublicacionUseCase cambiarEstadoPublicacionUseCase;
    private final ObtenerPublicacionUseCase obtenerPublicacionUseCase;
    private final ListarPublicacionesDeUsuarioUseCase listarPublicacionesDeUsuarioUseCase;
    private final ListarPublicacionesPorCategoriaUseCase listarPublicacionesPorCategoriaUseCase;
    private final BuscarPublicacionesCompatiblesUseCase buscarPublicacionesCompatiblesUseCase;

    public PublicacionesApiController(
            CrearCategoriaServicioUseCase crearCategoriaServicioUseCase,
            CambiarEstadoCategoriaServicioUseCase cambiarEstadoCategoriaServicioUseCase,
            ListarCategoriasActivasUseCase listarCategoriasActivasUseCase,
            CrearPublicacionUseCase crearPublicacionUseCase,
            ActualizarPublicacionUseCase actualizarPublicacionUseCase,
            CambiarEstadoPublicacionUseCase cambiarEstadoPublicacionUseCase,
            ObtenerPublicacionUseCase obtenerPublicacionUseCase,
            ListarPublicacionesDeUsuarioUseCase listarPublicacionesDeUsuarioUseCase,
            ListarPublicacionesPorCategoriaUseCase listarPublicacionesPorCategoriaUseCase,
            BuscarPublicacionesCompatiblesUseCase buscarPublicacionesCompatiblesUseCase
    ) {
        this.crearCategoriaServicioUseCase = crearCategoriaServicioUseCase;
        this.cambiarEstadoCategoriaServicioUseCase = cambiarEstadoCategoriaServicioUseCase;
        this.listarCategoriasActivasUseCase = listarCategoriasActivasUseCase;
        this.crearPublicacionUseCase = crearPublicacionUseCase;
        this.actualizarPublicacionUseCase = actualizarPublicacionUseCase;
        this.cambiarEstadoPublicacionUseCase = cambiarEstadoPublicacionUseCase;
        this.obtenerPublicacionUseCase = obtenerPublicacionUseCase;
        this.listarPublicacionesDeUsuarioUseCase = listarPublicacionesDeUsuarioUseCase;
        this.listarPublicacionesPorCategoriaUseCase = listarPublicacionesPorCategoriaUseCase;
        this.buscarPublicacionesCompatiblesUseCase = buscarPublicacionesCompatiblesUseCase;
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaServicioResult> crearCategoria(@RequestBody CrearCategoriaRequest request) {
        CategoriaServicioResult result = crearCategoriaServicioUseCase.crear(
                new CrearCategoriaServicioCommand(request.nombre, request.descripcion)
        );
        return ResponseEntity
                .created(URI.create("/api/v1/categorias/" + result.getId()))
                .body(result);
    }

    @PatchMapping("/categorias/{categoriaId}/estado")
    public ResponseEntity<CategoriaServicioResult> cambiarEstadoCategoria(
            @PathVariable UUID categoriaId,
            @RequestBody CambiarEstadoCategoriaRequest request
    ) {
        CategoriaServicioResult result = cambiarEstadoCategoriaServicioUseCase.cambiarEstado(
                new CambiarEstadoCategoriaServicioCommand(
                        categoriaId,
                        request.estadoDestino,
                        request.motivo
                )
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/categorias/activas")
    public ResponseEntity<List<CategoriaServicioResult>> listarCategoriasActivas() {
        return ResponseEntity.ok(listarCategoriasActivasUseCase.listarActivas());
    }

    @GetMapping("/categorias/{categoriaId}/publicaciones")
    public ResponseEntity<List<PublicacionServicioResult>> listarPublicacionesActivasPorCategoria(
            @PathVariable UUID categoriaId
    ) {
        return ResponseEntity.ok(listarPublicacionesPorCategoriaUseCase.listarActivasPorCategoria(categoriaId));
    }

    @PostMapping("/publicaciones")
    public ResponseEntity<PublicacionServicioResult> crearPublicacion(@RequestBody CrearPublicacionRequest request) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacion);
        List<Ubicacion> zonasCobertura = MvpWebMapper.toUbicaciones(request.zonasCobertura);
        List<DisponibilidadHoraria> disponibilidades = MvpWebMapper.toDisponibilidades(request.disponibilidadesHorarias);
        PublicacionServicioResult result = crearPublicacionUseCase.crear(
                new CrearPublicacionCommand(
                        request.usuarioId,
                        request.categoriaServicioId,
                        request.titulo,
                        request.descripcion,
                        request.modalidadServicio,
                        ubicacion,
                        zonasCobertura,
                        disponibilidades,
                        request.precioBase
                )
        );
        return ResponseEntity
                .created(URI.create("/api/v1/publicaciones/" + result.getId()))
                .body(result);
    }

    @PutMapping("/publicaciones/{publicacionId}")
    public ResponseEntity<PublicacionServicioResult> actualizarPublicacion(
            @PathVariable UUID publicacionId,
            @RequestBody ActualizarPublicacionRequest request
    ) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacion);
        List<Ubicacion> zonasCobertura = MvpWebMapper.toUbicaciones(request.zonasCobertura);
        List<DisponibilidadHoraria> disponibilidades = MvpWebMapper.toDisponibilidades(request.disponibilidadesHorarias);
        PublicacionServicioResult result = actualizarPublicacionUseCase.actualizar(
                new ActualizarPublicacionCommand(
                        publicacionId,
                        request.usuarioId,
                        request.categoriaServicioId,
                        request.titulo,
                        request.descripcion,
                        request.modalidadServicio,
                        ubicacion,
                        zonasCobertura,
                        disponibilidades,
                        request.precioBase
                )
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/publicaciones/{publicacionId}")
    public ResponseEntity<PublicacionServicioResult> obtenerPublicacion(@PathVariable UUID publicacionId) {
        return obtenerPublicacionUseCase.obtenerPorId(publicacionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuarios/{usuarioId}/publicaciones")
    public ResponseEntity<List<PublicacionServicioResult>> listarPublicacionesDeUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(listarPublicacionesDeUsuarioUseCase.listarPorUsuarioId(usuarioId));
    }

    @PatchMapping("/publicaciones/{publicacionId}/estado")
    public ResponseEntity<PublicacionServicioResult> cambiarEstadoPublicacion(
            @PathVariable UUID publicacionId,
            @RequestBody CambiarEstadoPublicacionRequest request
    ) {
        PublicacionServicioResult result = cambiarEstadoPublicacionUseCase.cambiarEstado(
                new CambiarEstadoPublicacionCommand(
                        publicacionId,
                        request.usuarioId,
                        request.estadoDestino,
                        request.motivo
                )
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/publicaciones/compatibles/busquedas")
    public ResponseEntity<List<PublicacionCompatibleResult>> buscarCompatibles(
            @RequestBody BuscarCompatiblesRequest request
    ) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacionRequerida);
        DisponibilidadHoraria disponibilidad = MvpWebMapper.toDisponibilidad(request.disponibilidadRequerida);
        List<PublicacionCompatibleResult> result = buscarPublicacionesCompatiblesUseCase.buscarCompatibles(
                new BuscarPublicacionesCompatiblesQuery(
                        request.solicitudServicioId,
                        request.categoriaServicioId,
                        request.descripcionNecesidad,
                        request.modalidadRequerida,
                        ubicacion,
                        disponibilidad,
                        request.precioMaximo,
                        request.radioBusquedaKm
                )
        );
        return ResponseEntity.ok(result);
    }

    public static class CrearCategoriaRequest {
        public String nombre;
        public String descripcion;
    }

    public static class CambiarEstadoCategoriaRequest {
        public EstadoCategoria estadoDestino;
        public String motivo;
    }

    public static class CrearPublicacionRequest {
        public UUID usuarioId;
        public UUID categoriaServicioId;
        public String titulo;
        public String descripcion;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public List<MvpWebMapper.UbicacionPayload> zonasCobertura;
        public List<MvpWebMapper.DisponibilidadPayload> disponibilidadesHorarias;
        public BigDecimal precioBase;
    }

    public static class ActualizarPublicacionRequest {
        public UUID usuarioId;
        public UUID categoriaServicioId;
        public String titulo;
        public String descripcion;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public List<MvpWebMapper.UbicacionPayload> zonasCobertura;
        public List<MvpWebMapper.DisponibilidadPayload> disponibilidadesHorarias;
        public BigDecimal precioBase;
    }

    public static class CambiarEstadoPublicacionRequest {
        public UUID usuarioId;
        public EstadoPublicacion estadoDestino;
        public String motivo;
    }

    public static class BuscarCompatiblesRequest {
        public UUID solicitudServicioId;
        public UUID categoriaServicioId;
        public String descripcionNecesidad;
        public ModalidadServicio modalidadRequerida;
        public MvpWebMapper.UbicacionPayload ubicacionRequerida;
        public MvpWebMapper.DisponibilidadPayload disponibilidadRequerida;
        public BigDecimal precioMaximo;
        public Double radioBusquedaKm;
    }
}
