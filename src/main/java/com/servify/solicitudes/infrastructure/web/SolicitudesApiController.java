package com.servify.solicitudes.infrastructure.web;

import com.servify.administracion.infrastructure.web.AdminAuthorizationService;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.shared.infrastructure.web.MvpWebMapper;
import com.servify.solicitudes.application.dto.AcordarPrecioAsignacionCommand;
import com.servify.solicitudes.application.dto.ActualizarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.dto.CalificarServicioCommand;
import com.servify.solicitudes.application.dto.CalificacionServicioResult;
import com.servify.solicitudes.application.dto.CancelarEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.CancelarRecurrenciaServicioCommand;
import com.servify.solicitudes.application.dto.CancelarSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.ConfirmarAsignacionSolicitudCommand;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.dto.ContraofertaResult;
import com.servify.solicitudes.application.dto.CrearSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.DistribucionSolicitudResult;
import com.servify.solicitudes.application.dto.EmitirContraofertaCommand;
import com.servify.solicitudes.application.dto.EstadoAsignacionSolicitudResult;
import com.servify.solicitudes.application.dto.ProponerEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ResolverContraofertaCommand;
import com.servify.solicitudes.application.dto.ResolverEncuentroServicioCommand;
import com.servify.solicitudes.application.dto.ResponderDistribucionSolicitudCommand;
import com.servify.solicitudes.application.dto.ServicioEncuentroResult;
import com.servify.solicitudes.application.dto.ServicioRecurrenciaResult;
import com.servify.solicitudes.application.dto.SolicitudRecibidaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.dto.TipoRespuestaDistribucion;
import com.servify.solicitudes.application.port.in.AcordarPrecioAsignacionUseCase;
import com.servify.solicitudes.application.port.in.ActualizarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.CalificarServicioUseCase;
import com.servify.solicitudes.application.port.in.CancelarEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.in.CancelarRecurrenciaServicioUseCase;
import com.servify.solicitudes.application.port.in.CancelarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.in.ConsultarCalificacionServicioUseCase;
import com.servify.solicitudes.application.port.in.CrearSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.EmitirContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ListarEncuentrosSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesDelSolicitanteUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesRecibidasDetalladasUseCase;
import com.servify.solicitudes.application.port.in.ObtenerEstadoAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ObtenerRecurrenciaSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ObtenerSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ProponerEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.in.ReintentarDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ResolverContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ResolverEncuentroServicioUseCase;
import com.servify.solicitudes.application.port.in.ResponderDistribucionSolicitudUseCase;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    private final AcordarPrecioAsignacionUseCase acordarPrecioAsignacionUseCase;
    private final ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase;
    private final CalificarServicioUseCase calificarServicioUseCase;
    private final ConsultarCalificacionServicioUseCase consultarCalificacionServicioUseCase;
    private final CancelarSolicitudServicioUseCase cancelarSolicitudServicioUseCase;
    private final ObtenerEstadoAsignacionSolicitudUseCase obtenerEstadoAsignacionSolicitudUseCase;
    private final ReintentarDistribucionSolicitudUseCase reintentarDistribucionSolicitudUseCase;
    private final ListarEncuentrosSolicitudUseCase listarEncuentrosSolicitudUseCase;
    private final ProponerEncuentroServicioUseCase proponerEncuentroServicioUseCase;
    private final ResolverEncuentroServicioUseCase resolverEncuentroServicioUseCase;
    private final CancelarEncuentroServicioUseCase cancelarEncuentroServicioUseCase;
    private final ObtenerRecurrenciaSolicitudUseCase obtenerRecurrenciaSolicitudUseCase;
    private final CancelarRecurrenciaServicioUseCase cancelarRecurrenciaServicioUseCase;
    private final AdminAuthorizationService authorizationService;

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
            AcordarPrecioAsignacionUseCase acordarPrecioAsignacionUseCase,
            ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase,
            CalificarServicioUseCase calificarServicioUseCase,
            ConsultarCalificacionServicioUseCase consultarCalificacionServicioUseCase,
            CancelarSolicitudServicioUseCase cancelarSolicitudServicioUseCase,
            ObtenerEstadoAsignacionSolicitudUseCase obtenerEstadoAsignacionSolicitudUseCase,
            ReintentarDistribucionSolicitudUseCase reintentarDistribucionSolicitudUseCase,
            ListarEncuentrosSolicitudUseCase listarEncuentrosSolicitudUseCase,
            ProponerEncuentroServicioUseCase proponerEncuentroServicioUseCase,
            ResolverEncuentroServicioUseCase resolverEncuentroServicioUseCase,
            CancelarEncuentroServicioUseCase cancelarEncuentroServicioUseCase,
            ObtenerRecurrenciaSolicitudUseCase obtenerRecurrenciaSolicitudUseCase,
            CancelarRecurrenciaServicioUseCase cancelarRecurrenciaServicioUseCase,
            AdminAuthorizationService authorizationService
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
        this.acordarPrecioAsignacionUseCase = acordarPrecioAsignacionUseCase;
        this.confirmarFinalizacionServicioUseCase = confirmarFinalizacionServicioUseCase;
        this.calificarServicioUseCase = calificarServicioUseCase;
        this.consultarCalificacionServicioUseCase = consultarCalificacionServicioUseCase;
        this.cancelarSolicitudServicioUseCase = cancelarSolicitudServicioUseCase;
        this.obtenerEstadoAsignacionSolicitudUseCase = obtenerEstadoAsignacionSolicitudUseCase;
        this.reintentarDistribucionSolicitudUseCase = reintentarDistribucionSolicitudUseCase;
        this.listarEncuentrosSolicitudUseCase = listarEncuentrosSolicitudUseCase;
        this.proponerEncuentroServicioUseCase = proponerEncuentroServicioUseCase;
        this.resolverEncuentroServicioUseCase = resolverEncuentroServicioUseCase;
        this.cancelarEncuentroServicioUseCase = cancelarEncuentroServicioUseCase;
        this.obtenerRecurrenciaSolicitudUseCase = obtenerRecurrenciaSolicitudUseCase;
        this.cancelarRecurrenciaServicioUseCase = cancelarRecurrenciaServicioUseCase;
        this.authorizationService = authorizationService;
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
                        request.precioReferencia,
                        request.tipoProgramacion,
                        request.fechaProgramadaInicio,
                        request.fechaProgramadaFin,
                        request.frecuenciaRecurrencia,
                        request.fechaInicioRecurrencia,
                        request.fechaFinRecurrencia
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
                        request.precioReferencia,
                        request.tipoProgramacion,
                        request.fechaProgramadaInicio,
                        request.fechaProgramadaFin
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

    @GetMapping("/solicitudes/{solicitudId}/encuentros")
    public ResponseEntity<List<ServicioEncuentroResult>> listarEncuentros(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(listarEncuentrosSolicitudUseCase.listarPorSolicitudId(solicitudId));
    }

    @PostMapping("/solicitudes/{solicitudId}/encuentros")
    public ResponseEntity<ServicioEncuentroResult> proponerEncuentro(
            @PathVariable UUID solicitudId,
            @RequestBody ProponerEncuentroRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.propuestoPorId, "propone el encuentro");
        ServicioEncuentroResult result = proponerEncuentroServicioUseCase.proponer(
                new ProponerEncuentroServicioCommand(
                        solicitudId,
                        request.asignacionServicioId,
                        request.propuestoPorId,
                        request.fechaInicio,
                        request.fechaFin,
                        request.mensaje
                )
        );
        return ResponseEntity.created(URI.create("/api/v1/encuentros/" + result.getId())).body(result);
    }

    @PostMapping("/encuentros/{encuentroId}/resoluciones")
    public ResponseEntity<ServicioEncuentroResult> resolverEncuentro(
            @PathVariable UUID encuentroId,
            @RequestBody ResolverEncuentroRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.usuarioId, "resuelve el encuentro");
        ServicioEncuentroResult result = resolverEncuentroServicioUseCase.resolver(
                new ResolverEncuentroServicioCommand(
                        encuentroId,
                        request.usuarioId,
                        request.decision
                )
        );
        return ResponseEntity.ok(result);
    }

    @PutMapping("/solicitudes/{solicitudId}/asignaciones/{asignacionServicioId}/precio")
    public ResponseEntity<AsignacionServicioResult> acordarPrecioAsignacion(
            @PathVariable UUID solicitudId,
            @PathVariable UUID asignacionServicioId,
            @RequestBody AcordarPrecioAsignacionRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.solicitanteId, "acuerda el precio");
        return ResponseEntity.ok(acordarPrecioAsignacionUseCase.acordar(
                new AcordarPrecioAsignacionCommand(
                        solicitudId,
                        asignacionServicioId,
                        request.solicitanteId,
                        request.precioAcordado
                )
        ));
    }

    @DeleteMapping("/encuentros/{encuentroId}")
    public ResponseEntity<ServicioEncuentroResult> cancelarEncuentro(
            @PathVariable UUID encuentroId,
            @RequestBody CancelarEncuentroRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.usuarioId, "cancela el encuentro");
        return ResponseEntity.ok(cancelarEncuentroServicioUseCase.cancelar(
                new CancelarEncuentroServicioCommand(encuentroId, request.usuarioId)
        ));
    }

    @GetMapping("/solicitudes/{solicitudId}/recurrencia")
    public ResponseEntity<ServicioRecurrenciaResult> obtenerRecurrencia(@PathVariable UUID solicitudId) {
        return obtenerRecurrenciaSolicitudUseCase.obtenerPorSolicitudId(solicitudId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/solicitudes/{solicitudId}/recurrencia")
    public ResponseEntity<ServicioRecurrenciaResult> cancelarRecurrencia(
            @PathVariable UUID solicitudId,
            @RequestBody CancelarRecurrenciaRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.usuarioId, "cancela la recurrencia");
        return ResponseEntity.ok(cancelarRecurrenciaServicioUseCase.cancelar(
                new CancelarRecurrenciaServicioCommand(solicitudId, request.usuarioId, request.motivo)
        ));
    }

    @PostMapping("/solicitudes/{solicitudId}/finalizaciones/confirmaciones")
    public ResponseEntity<Void> confirmarFinalizacion(
            @PathVariable UUID solicitudId,
            @RequestBody ConfirmarFinalizacionRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.confirmanteId, "confirma el servicio");
        confirmarFinalizacionServicioUseCase.confirmar(
                new ConfirmarFinalizacionServicioCommand(
                        solicitudId,
                        request.asignacionServicioId,
                        request.encuentroId,
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
                        request.rolCalificador,
                        request.comentario
                )
        );
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + solicitudId + "/calificaciones")).build();
    }

    @GetMapping("/solicitudes/{solicitudId}/calificaciones")
    public ResponseEntity<CalificacionServicioResult> obtenerCalificacion(
            @PathVariable UUID solicitudId,
            @RequestParam UUID asignacionServicioId,
            @RequestParam RolConfirmante rolCalificador
    ) {
        return consultarCalificacionServicioUseCase
                .obtenerPorAsignacionYRol(solicitudId, asignacionServicioId, rolCalificador)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<Void> cancelarSolicitud(
            @PathVariable UUID solicitudId,
            @RequestBody CancelarSolicitudRequest request,
            HttpServletRequest httpRequest
    ) {
        exigirActorAutenticado(httpRequest, request.solicitanteId, "cancela la solicitud");
        cancelarSolicitudServicioUseCase.cancelar(
                new CancelarSolicitudServicioCommand(solicitudId, request.solicitanteId)
        );
        return ResponseEntity.noContent().build();
    }

    private void exigirActorAutenticado(HttpServletRequest httpRequest, UUID actorDeclarado, String accion) {
        UUID autenticadoId = authorizationService.requireActiveUser(httpRequest).getId();
        if (actorDeclarado == null || !autenticadoId.equals(actorDeclarado)) {
            throw new ForbiddenException("La identidad autenticada no coincide con quien " + accion);
        }
    }

    public static class CrearSolicitudRequest {
        public UUID solicitanteId;
        public UUID categoriaServicioId;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public MvpWebMapper.DisponibilidadPayload disponibilidadRequerida;
        public String descripcionNecesidad;
        public BigDecimal precioReferencia;
        public TipoProgramacionSolicitud tipoProgramacion;
        public LocalDateTime fechaProgramadaInicio;
        public LocalDateTime fechaProgramadaFin;
        public FrecuenciaRecurrencia frecuenciaRecurrencia;
        public LocalDate fechaInicioRecurrencia;
        public LocalDate fechaFinRecurrencia;
    }

    public static class ActualizarSolicitudRequest {
        public UUID solicitanteId;
        public ModalidadServicio modalidadServicio;
        public MvpWebMapper.UbicacionPayload ubicacion;
        public MvpWebMapper.DisponibilidadPayload disponibilidadRequerida;
        public String descripcionNecesidad;
        public BigDecimal precioReferencia;
        public TipoProgramacionSolicitud tipoProgramacion;
        public LocalDateTime fechaProgramadaInicio;
        public LocalDateTime fechaProgramadaFin;
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

    public static class AcordarPrecioAsignacionRequest {
        public UUID solicitanteId;
        public BigDecimal precioAcordado;
    }

    public static class ConfirmarFinalizacionRequest {
        public UUID asignacionServicioId;
        public UUID encuentroId;
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
        public String comentario;
    }

    public static class CancelarSolicitudRequest {
        public UUID solicitanteId;
    }

    public static class ProponerEncuentroRequest {
        public UUID asignacionServicioId;
        public UUID propuestoPorId;
        public LocalDateTime fechaInicio;
        public LocalDateTime fechaFin;
        public String mensaje;
    }

    public static class ResolverEncuentroRequest {
        public UUID usuarioId;
        public TipoDecisionSolicitud decision;
    }

    public static class CancelarEncuentroRequest {
        public UUID usuarioId;
    }

    public static class CancelarRecurrenciaRequest {
        public UUID usuarioId;
        public String motivo;
    }
}
