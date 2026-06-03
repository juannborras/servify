package com.servify.solicitudes.infrastructure.web;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.shared.infrastructure.web.MvpWebMapper;
import com.servify.solicitudes.application.dto.ActualizarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.dto.CalificarServicioCommand;
import com.servify.solicitudes.application.dto.CancelarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.ConfirmarAsignacionSolicitudCommand;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.dto.ContraofertaResult;
import com.servify.solicitudes.application.dto.CrearSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;
import com.servify.solicitudes.application.dto.EmitirContraofertaCommand;
import com.servify.solicitudes.application.dto.EstadoAsignacionSolicitudResult;
import com.servify.solicitudes.application.dto.ResolverContraofertaCommand;
import com.servify.solicitudes.application.dto.ResponderDistribucionSolicitudCommand;
import com.servify.solicitudes.application.dto.SolicitudRecibidaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.dto.TipoRespuestaDistribucion;
import com.servify.solicitudes.application.port.in.ActualizarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.CalificarServicioUseCase;
import com.servify.solicitudes.application.port.in.CancelarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.in.CrearSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.EmitirContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesDelSolicitanteUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesRecibidasDetalladasUseCase;
import com.servify.solicitudes.application.port.in.ObtenerEstadoAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ObtenerSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ReintentarDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ResolverContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ResponderDistribucionSolicitudUseCase;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SolicitudesApiController {

    private final CrearSolicitudServicioUseCase crearSolicitudServicioUseCase;
    private final ActualizarSolicitudServicioUseCase actualizarSolicitudServicioUseCase;
    private final ObtenerSolicitudServicioUseCase obtenerSolicitudServicioUseCase;
    private final ListarSolicitudesDelSolicitanteUseCase listarSolicitudesDelSolicitanteUseCase;
    private final ListarSolicitudesRecibidasDetalladasUseCase listarSolicitudesRecibidasDetalladasUseCase;
    private final ResponderDistribucionSolicitudUseCase responderDistribucionSolicitudUseCase;
    private final EmitirContraofertaUseCase emitirContraofertaUseCase;
    private final ResolverContraofertaUseCase resolverContraofertaUseCase;
    private final ConfirmarAsignacionSolicitudUseCase confirmarAsignacionSolicitudUseCase;
    private final ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase;
    private final CalificarServicioUseCase calificarServicioUseCase;
    private final CancelarSolicitudServicioUseCase cancelarSolicitudServicioUseCase;
    private final ObtenerEstadoAsignacionSolicitudUseCase obtenerEstadoAsignacionSolicitudUseCase;
    private final ReintentarDistribucionSolicitudUseCase reintentarDistribucionSolicitudUseCase;

    public SolicitudesApiController(
            CrearSolicitudServicioUseCase crearSolicitudServicioUseCase,
            ActualizarSolicitudServicioUseCase actualizarSolicitudServicioUseCase,
            ObtenerSolicitudServicioUseCase obtenerSolicitudServicioUseCase,
            ListarSolicitudesDelSolicitanteUseCase listarSolicitudesDelSolicitanteUseCase,
            ListarSolicitudesRecibidasDetalladasUseCase listarSolicitudesRecibidasDetalladasUseCase,
            ResponderDistribucionSolicitudUseCase responderDistribucionSolicitudUseCase,
            EmitirContraofertaUseCase emitirContraofertaUseCase,
            ResolverContraofertaUseCase resolverContraofertaUseCase,
            ConfirmarAsignacionSolicitudUseCase confirmarAsignacionSolicitudUseCase,
            ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase,
            CalificarServicioUseCase calificarServicioUseCase,
            CancelarSolicitudServicioUseCase cancelarSolicitudServicioUseCase,
            ObtenerEstadoAsignacionSolicitudUseCase obtenerEstadoAsignacionSolicitudUseCase,
            ReintentarDistribucionSolicitudUseCase reintentarDistribucionSolicitudUseCase
    ) {
        this.crearSolicitudServicioUseCase = crearSolicitudServicioUseCase;
        this.actualizarSolicitudServicioUseCase = actualizarSolicitudServicioUseCase;
        this.obtenerSolicitudServicioUseCase = obtenerSolicitudServicioUseCase;
        this.listarSolicitudesDelSolicitanteUseCase = listarSolicitudesDelSolicitanteUseCase;
        this.listarSolicitudesRecibidasDetalladasUseCase = listarSolicitudesRecibidasDetalladasUseCase;
        this.responderDistribucionSolicitudUseCase = responderDistribucionSolicitudUseCase;
        this.emitirContraofertaUseCase = emitirContraofertaUseCase;
        this.resolverContraofertaUseCase = resolverContraofertaUseCase;
        this.confirmarAsignacionSolicitudUseCase = confirmarAsignacionSolicitudUseCase;
        this.confirmarFinalizacionServicioUseCase = confirmarFinalizacionServicioUseCase;
        this.calificarServicioUseCase = calificarServicioUseCase;
        this.cancelarSolicitudServicioUseCase = cancelarSolicitudServicioUseCase;
        this.obtenerEstadoAsignacionSolicitudUseCase = obtenerEstadoAsignacionSolicitudUseCase;
        this.reintentarDistribucionSolicitudUseCase = reintentarDistribucionSolicitudUseCase;
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudServicioResult> crearSolicitud(@RequestBody CrearSolicitudRequest request) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacion);
        DisponibilidadHoraria disponibilidad = MvpWebMapper.toDisponibilidad(request.disponibilidadRequerida);
        SolicitudServicioResult result = crearSolicitudServicioUseCase.crear(
                new CrearSolicitudServicioCommand(
                        request.solicitanteId,
                        request.categoriaServicioId,
                        request.modalidadServicio,
                        ubicacion,
                        disponibilidad,
                        request.descripcionNecesidad,
                        request.precioReferencia
                )
        );
        return ResponseEntity
                .created(URI.create("/api/v1/solicitudes/" + result.getId()))
                .body(result);
    }

    @GetMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<SolicitudServicioResult> obtenerSolicitud(@PathVariable UUID solicitudId) {
        return obtenerSolicitudServicioUseCase.obtenerPorId(solicitudId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<SolicitudServicioResult> actualizarSolicitud(
            @PathVariable UUID solicitudId,
            @RequestBody ActualizarSolicitudRequest request
    ) {
        Ubicacion ubicacion = MvpWebMapper.toUbicacion(request.ubicacion);
        DisponibilidadHoraria disponibilidad = MvpWebMapper.toDisponibilidad(request.disponibilidadRequerida);
        SolicitudServicioResult result = actualizarSolicitudServicioUseCase.actualizar(
                new ActualizarSolicitudServicioCommand(
                        solicitudId,
                        request.solicitanteId,
                        request.modalidadServicio,
                        ubicacion,
                        disponibilidad,
                        request.descripcionNecesidad,
                        request.precioReferencia
                )
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/usuarios/{solicitanteId}/solicitudes")
    public ResponseEntity<List<SolicitudServicioResult>> listarSolicitudesDelSolicitante(
            @PathVariable UUID solicitanteId
    ) {
        return ResponseEntity.ok(listarSolicitudesDelSolicitanteUseCase.listarPorSolicitanteId(solicitanteId));
    }

    @GetMapping("/prestadores/{prestadorId}/solicitudes-recibidas")
    public ResponseEntity<List<SolicitudRecibidaResult>> listarSolicitudesRecibidas(
            @PathVariable UUID prestadorId
    ) {
        return ResponseEntity.ok(listarSolicitudesRecibidasDetalladasUseCase.listarPorPrestadorId(prestadorId));
    }

    @PostMapping("/distribuciones/{distribucionSolicitudId}/respuestas")
    public ResponseEntity<Void> responderDistribucion(
            @PathVariable UUID distribucionSolicitudId,
            @RequestBody ResponderDistribucionRequest request
    ) {
        responderDistribucionSolicitudUseCase.responder(
                new ResponderDistribucionSolicitudCommand(
                        distribucionSolicitudId,
                        request.prestadorId,
                        request.tipoRespuesta
                )
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/distribuciones/{distribucionSolicitudId}/contraofertas")
    public ResponseEntity<Void> emitirContraoferta(
            @PathVariable UUID distribucionSolicitudId,
            @RequestBody EmitirContraofertaRequest request
    ) {
        emitirContraofertaUseCase.emitir(
                new EmitirContraofertaCommand(
                        distribucionSolicitudId,
                        request.prestadorId,
                        request.precioPropuesto,
                        request.mensaje
                )
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/contraofertas/{contraofertaId}/resoluciones")
    public ResponseEntity<ContraofertaResult> resolverContraoferta(
            @PathVariable UUID contraofertaId,
            @RequestBody ResolverContraofertaRequest request
    ) {
        ContraofertaResult result = resolverContraofertaUseCase.resolver(
                new ResolverContraofertaCommand(
                        contraofertaId,
                        request.solicitanteId,
                        request.decision
                )
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/solicitudes/{solicitudId}/asignaciones/confirmaciones")
    public ResponseEntity<AsignacionServicioResult> confirmarAsignacion(
            @PathVariable UUID solicitudId,
            @RequestBody ConfirmarAsignacionRequest request
    ) {
        AsignacionServicioResult result = confirmarAsignacionSolicitudUseCase.confirmar(
                new ConfirmarAsignacionSolicitudCommand(
                        solicitudId,
                        request.distribucionSolicitudId,
                        request.solicitanteId
                )
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/solicitudes/{solicitudId}/estado-asignacion")
    public ResponseEntity<EstadoAsignacionSolicitudResult> obtenerEstadoAsignacion(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(obtenerEstadoAsignacionSolicitudUseCase.obtenerEstado(solicitudId));
    }

    @PostMapping("/solicitudes/{solicitudId}/distribuciones/reintentos")
    public ResponseEntity<List<DistribucionSolicitudResult>> reintentarDistribucion(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(reintentarDistribucionSolicitudUseCase.reintentar(solicitudId));
    }

    @PostMapping("/solicitudes/{solicitudId}/finalizaciones/confirmaciones")
    public ResponseEntity<Void> confirmarFinalizacion(
            @PathVariable UUID solicitudId,
            @RequestBody ConfirmarFinalizacionRequest request
    ) {
        confirmarFinalizacionServicioUseCase.confirmar(
                new ConfirmarFinalizacionServicioCommand(
                        solicitudId,
                        request.asignacionServicioId,
                        request.confirmanteId,
                        request.rolConfirmante,
                        request.observacion
                )
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitudes/{solicitudId}/calificaciones")
    public ResponseEntity<Void> calificar(
            @PathVariable UUID solicitudId,
            @RequestBody CalificarRequest request
    ) {
        calificarServicioUseCase.calificar(
                new CalificarServicioCommand(
                        solicitudId,
                        request.asignacionServicioId,
                        request.solicitanteId,
                        request.prestadorId,
                        request.puntaje,
                        request.calificadorId,
                        request.rolCalificador
                )
        );
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + solicitudId + "/calificaciones")).build();
    }

    @DeleteMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<Void> cancelarSolicitud(
            @PathVariable UUID solicitudId,
            @RequestBody CancelarSolicitudRequest request
    ) {
        cancelarSolicitudServicioUseCase.cancelar(
                new CancelarSolicitudServicioCommand(solicitudId, request.solicitanteId)
        );
        return ResponseEntity.noContent().build();
    }

    public static class CrearSolicitudRequest {
        public UUID solicitanteId;
        public UUID categoriaServicioId;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public MvpWebMapper.DisponibilidadPayload disponibilidadRequerida;
        public String descripcionNecesidad;
        public BigDecimal precioReferencia;
    }

    public static class ActualizarSolicitudRequest {
        public UUID solicitanteId;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public MvpWebMapper.DisponibilidadPayload disponibilidadRequerida;
        public String descripcionNecesidad;
        public BigDecimal precioReferencia;
    }

    public static class ResponderDistribucionRequest {
        public UUID prestadorId;
        public TipoRespuestaDistribucion tipoRespuesta;
    }

    public static class EmitirContraofertaRequest {
        public UUID prestadorId;
        public BigDecimal precioPropuesto;
        public String mensaje;
    }

    public static class ResolverContraofertaRequest {
        public UUID solicitanteId;
        public TipoDecisionSolicitud decision;
    }

    public static class ConfirmarAsignacionRequest {
        public UUID distribucionSolicitudId;
        public UUID solicitanteId;
    }

    public static class ConfirmarFinalizacionRequest {
        public UUID asignacionServicioId;
        public UUID confirmanteId;
        public RolConfirmante rolConfirmante;
        public String observacion;
    }

    public static class CalificarRequest {
        public UUID asignacionServicioId;
        public UUID solicitanteId;
        public UUID prestadorId;
        public UUID calificadorId;
        public RolConfirmante rolCalificador;
        public Integer puntaje;
    }

    public static class CancelarSolicitudRequest {
        public UUID solicitanteId;
    }
}
