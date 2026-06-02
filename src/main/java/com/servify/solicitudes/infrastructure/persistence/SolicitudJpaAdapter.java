package com.servify.solicitudes.infrastructure.persistence;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.port.out.*;
import com.servify.solicitudes.domain.enumtype.*;
import com.servify.solicitudes.domain.model.*;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ── SolicitudServicioJpaAdapterImpl ──────────────────────────
@Component
class SolicitudServicioJpaAdapterImpl implements SolicitudServicioRepositoryPort {

    final SolicitudServicioJpaRepository solicitudRepo;
    final AsignacionServicioJpaRepository asignacionRepo;

    SolicitudServicioJpaAdapterImpl(SolicitudServicioJpaRepository solicitudRepo,
                                     AsignacionServicioJpaRepository asignacionRepo) {
        this.solicitudRepo = solicitudRepo;
        this.asignacionRepo = asignacionRepo;
    }

    @Override
    public SolicitudServicio guardar(SolicitudServicio s) {
        SolicitudServicioJpaEntity e = toEntity(s);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return toDomain(solicitudRepo.save(e));
    }

    @Override
    public Optional<SolicitudServicio> buscarPorId(UUID solicitudId) {
        return solicitudRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(solicitudId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public List<SolicitudServicio> buscarPorSolicitanteId(UUID solicitanteId) {
        return solicitudRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getSolicitanteId()).equals(solicitanteId))
                .map(this::toDomain).toList();
    }

    SolicitudServicioJpaEntity toEntity(SolicitudServicio s) {
        SolicitudServicioJpaEntity e = new SolicitudServicioJpaEntity();
        if (s.getId() != null) {
            solicitudRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(s.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setSolicitanteId(longFromUuid(s.getSolicitanteId()));
        e.setCategoriaId(longFromUuid(s.getCategoriaServicioId()));
        e.setDescripcion(s.getDescripcionNecesidad());
        e.setModalidad(modalidadToDb(s.getModalidadServicio()));
        if (s.getUbicacion() != null) {
            e.setPais(s.getUbicacion().getPais()); e.setProvincia(s.getUbicacion().getProvincia());
            e.setCiudad(s.getUbicacion().getCiudad()); e.setLocalidad(s.getUbicacion().getLocalidad());
            e.setCalle(s.getUbicacion().getCalle()); e.setAltura(s.getUbicacion().getAltura());
            e.setReferencia(s.getUbicacion().getReferencia());
            e.setLatitud(s.getUbicacion().getLatitud()); e.setLongitud(s.getUbicacion().getLongitud());
        }
        if (s.getDisponibilidadRequerida() != null) {
            DisponibilidadHoraria d = s.getDisponibilidadRequerida();
            e.setDisponibilidadDia(d.getDiaSemana() != null ? d.getDiaSemana().name().toLowerCase() : null);
            e.setDisponibilidadHoraDesde(d.getHoraDesde());
            e.setDisponibilidadHoraHasta(d.getHoraHasta());
        }
        e.setPrecioReferencia(s.getPrecioReferencia());
        e.setEstado(s.getEstado() != null ? s.getEstado().name().toLowerCase() : null);
        e.setFechaSolicitud(s.getFechaSolicitud());
        return e;
    }

    SolicitudServicio toDomain(SolicitudServicioJpaEntity e) {
        Ubicacion ub = new Ubicacion(e.getPais(), e.getProvincia(), e.getCiudad(), e.getLocalidad(),
                e.getCalle(), e.getAltura(), e.getReferencia(), e.getLatitud(), e.getLongitud());
        DisponibilidadHoraria disp = e.getDisponibilidadDia() != null
                ? new DisponibilidadHoraria(DayOfWeek.valueOf(e.getDisponibilidadDia().toUpperCase()),
                e.getDisponibilidadHoraDesde(), e.getDisponibilidadHoraHasta()) : null;
        SolicitudServicio s = new SolicitudServicio(
                UsuarioJpaAdapter.uuidFromLong(e.getId()),
                UsuarioJpaAdapter.uuidFromLong(e.getSolicitanteId()),
                UsuarioJpaAdapter.uuidFromLong(e.getCategoriaId()),
                modalidadFromDb(e.getModalidad()), ub, disp,
                e.getDescripcion(), e.getPrecioReferencia(),
                EstadoSolicitud.valueOf(e.getEstado().toUpperCase()), e.getFechaSolicitud());
        if (e.getCreatedAt() != null) s.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) s.marcarModificacion(e.getUpdatedAt());
        return s;
    }

    static String modalidadToDb(ModalidadServicio m) {
        if (m == null) return null;
        return switch (m) { case PRESENCIAL -> "presencial"; case VIRTUAL -> "virtual"; case MIXTA -> "mixta"; };
    }

    static ModalidadServicio modalidadFromDb(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "presencial" -> ModalidadServicio.PRESENCIAL;
            case "virtual" -> ModalidadServicio.VIRTUAL;
            case "mixta", "ambas" -> ModalidadServicio.MIXTA;
            default -> throw new IllegalArgumentException("Modalidad desconocida: " + s);
        };
    }

    static Long longFromUuid(UUID uuid) { return uuid == null ? null : uuid.getLeastSignificantBits(); }
}

// ── DistribucionSolicitudJpaAdapterImpl ──────────────────────
@Component
class DistribucionSolicitudJpaAdapterImpl implements DistribucionSolicitudRepositoryPort {

    private final DistribucionSolicitudJpaRepository distribucionRepo;

    DistribucionSolicitudJpaAdapterImpl(DistribucionSolicitudJpaRepository distribucionRepo) {
        this.distribucionRepo = distribucionRepo;
    }

    @Override
    public DistribucionSolicitud guardar(DistribucionSolicitud d) {
        DistribucionSolicitudJpaEntity e = toEntity(d);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        return toDomain(distribucionRepo.save(e));
    }

    @Override
    public Optional<DistribucionSolicitud> buscarPorId(UUID distribucionId) {
        return distribucionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(distribucionId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public List<DistribucionSolicitud> buscarPorSolicitudId(UUID solicitudId) {
        return distribucionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getSolicitudId()).equals(solicitudId))
                .map(this::toDomain).toList();
    }

    @Override
    public List<DistribucionSolicitud> buscarPorPrestadorId(UUID prestadorId) {
        return distribucionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()).equals(prestadorId))
                .map(this::toDomain).toList();
    }

    @Override
    public List<DistribucionSolicitud> buscarActivasPorSolicitudId(UUID solicitudId) {
        return buscarPorSolicitudId(solicitudId).stream()
                .filter(d -> d.estaEnviada() || d.estaAceptada() || d.estaContraofertada()).toList();
    }

    DistribucionSolicitudJpaEntity toEntity(DistribucionSolicitud d) {
        DistribucionSolicitudJpaEntity e = new DistribucionSolicitudJpaEntity();
        if (d.getId() != null) {
            distribucionRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(d.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setSolicitudId(SolicitudServicioJpaAdapterImpl.longFromUuid(d.getSolicitudId()));
        e.setPublicacionId(SolicitudServicioJpaAdapterImpl.longFromUuid(d.getPublicacionServicioId()));
        e.setPrestadorId(SolicitudServicioJpaAdapterImpl.longFromUuid(d.getPrestadorId()));
        e.setEstado(d.getEstado() != null ? d.getEstado().name().toLowerCase() : null);
        e.setRondaDistribucion(d.getRondaDistribucion());
        e.setFechaEnvio(d.getFechaEnvio());
        e.setFechaRespuesta(d.getFechaRespuesta());
        e.setFechaExpiracion(d.getFechaExpiracion());
        return e;
    }

    DistribucionSolicitud toDomain(DistribucionSolicitudJpaEntity e) {
        DistribucionSolicitud d = new DistribucionSolicitud(
                UsuarioJpaAdapter.uuidFromLong(e.getId()),
                UsuarioJpaAdapter.uuidFromLong(e.getSolicitudId()),
                UsuarioJpaAdapter.uuidFromLong(e.getPublicacionId()),
                UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()),
                EstadoDistribucion.valueOf(e.getEstado().toUpperCase()),
                e.getRondaDistribucion(), e.getFechaEnvio(), e.getFechaRespuesta(), e.getFechaExpiracion());
        if (e.getCreatedAt() != null) d.marcarCreacion(e.getCreatedAt());
        return d;
    }
}

// ── AsignacionServicioJpaAdapterImpl ─────────────────────────
@Component
class AsignacionServicioJpaAdapterImpl implements AsignacionServicioRepositoryPort {

    private final AsignacionServicioJpaRepository asignacionRepo;
    private final SolicitudServicioJpaRepository solicitudRepo;

    AsignacionServicioJpaAdapterImpl(AsignacionServicioJpaRepository asignacionRepo,
                                      SolicitudServicioJpaRepository solicitudRepo) {
        this.asignacionRepo = asignacionRepo;
        this.solicitudRepo = solicitudRepo;
    }

    @Override
    public AsignacionServicio guardar(AsignacionServicio a) {
        AsignacionServicioJpaEntity e = toEntity(a);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return toDomain(asignacionRepo.save(e));
    }

    @Override
    public Optional<AsignacionServicio> buscarPorId(UUID asignacionId) {
        return asignacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(asignacionId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public Optional<AsignacionServicio> buscarPorSolicitudId(UUID solicitudId) {
        return asignacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getSolicitudId()).equals(solicitudId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public List<AsignacionServicio> buscarPorPrestadorId(UUID prestadorId) {
        return asignacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()).equals(prestadorId))
                .map(this::toDomain).toList();
    }

    @Override
    public List<AsignacionServicio> buscarPorSolicitanteId(UUID solicitanteId) {
        return asignacionRepo.findAll().stream()
                .filter(e -> {
                    Optional<SolicitudServicioJpaEntity> s = solicitudRepo.findAll().stream()
                            .filter(se -> se.getId().equals(e.getSolicitudId())).findFirst();
                    return s.map(se -> UsuarioJpaAdapter.uuidFromLong(se.getSolicitanteId()).equals(solicitanteId)).orElse(false);
                }).map(this::toDomain).toList();
    }

    AsignacionServicioJpaEntity toEntity(AsignacionServicio a) {
        AsignacionServicioJpaEntity e = new AsignacionServicioJpaEntity();
        if (a.getId() != null) {
            asignacionRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(a.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setSolicitudId(SolicitudServicioJpaAdapterImpl.longFromUuid(a.getSolicitudId()));
        e.setDistribucionId(SolicitudServicioJpaAdapterImpl.longFromUuid(a.getDistribucionSolicitudId()));
        e.setPrestadorId(SolicitudServicioJpaAdapterImpl.longFromUuid(a.getPrestadorId()));
        e.setPublicacionId(SolicitudServicioJpaAdapterImpl.longFromUuid(a.getPublicacionServicioId()));
        e.setPrecioAcordado(a.getPrecioAcordado());
        e.setEstado(a.getEstado() != null ? a.getEstado().name().toLowerCase() : null);
        e.setFechaAsignacion(a.getFechaAsignacion());
        e.setFechaFinalizacion(a.getFechaFinalizacion());
        return e;
    }

    AsignacionServicio toDomain(AsignacionServicioJpaEntity e) {
        AsignacionServicio a = new AsignacionServicio(
                UsuarioJpaAdapter.uuidFromLong(e.getId()),
                UsuarioJpaAdapter.uuidFromLong(e.getSolicitudId()),
                UsuarioJpaAdapter.uuidFromLong(e.getDistribucionId()),
                UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()),
                UsuarioJpaAdapter.uuidFromLong(e.getPublicacionId()),
                e.getPrecioAcordado(),
                EstadoAsignacion.valueOf(e.getEstado().toUpperCase()),
                e.getFechaAsignacion(), e.getFechaFinalizacion());
        if (e.getCreatedAt() != null) a.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) a.marcarModificacion(e.getUpdatedAt());
        return a;
    }
}

// ── CalificacionJpaAdapterImpl ───────────────────────────────
@Component
class CalificacionJpaAdapterImpl implements CalificacionRepositoryPort {

    private final CalificacionJpaRepository calificacionRepo;
    private final AsignacionServicioJpaRepository asignacionRepo;
    private final SolicitudServicioJpaRepository solicitudRepo;

    CalificacionJpaAdapterImpl(CalificacionJpaRepository calificacionRepo,
                                AsignacionServicioJpaRepository asignacionRepo,
                                SolicitudServicioJpaRepository solicitudRepo) {
        this.calificacionRepo = calificacionRepo;
        this.asignacionRepo = asignacionRepo;
        this.solicitudRepo = solicitudRepo;
    }

    @Override
    public Calificacion guardar(Calificacion c) {
        CalificacionJpaEntity e = toEntity(c);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        return toDomain(calificacionRepo.save(e));
    }

    @Override
    public Optional<Calificacion> buscarPorId(UUID calificacionId) {
        return calificacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(calificacionId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public Optional<Calificacion> buscarPorSolicitudId(UUID solicitudId) {
        return asignacionRepo.findAll().stream()
                .filter(a -> UsuarioJpaAdapter.uuidFromLong(a.getSolicitudId()).equals(solicitudId))
                .findFirst()
                .flatMap(a -> calificacionRepo.findByAsignacionId(a.getId()).stream().findFirst())
                .map(this::toDomain);
    }

    @Override
    public Optional<Calificacion> buscarPorAsignacionServicioIdYRolCalificador(
            UUID asignacionServicioId,
            RolConfirmante rolCalificador
    ) {
        if (asignacionServicioId == null || rolCalificador == null) {
            return Optional.empty();
        }
        return asignacionRepo.findAll().stream()
                .filter(a -> UsuarioJpaAdapter.uuidFromLong(a.getId()).equals(asignacionServicioId))
                .findFirst()
                .flatMap(a -> calificacionRepo.findByAsignacionIdAndRolCalificador(
                        a.getId(),
                        rolCalificador.name().toLowerCase()
                ))
                .map(this::toDomain);
    }

    @Override
    public List<Calificacion> buscarPorPrestadorId(UUID prestadorId) {
        return calificacionRepo.findAll().stream()
                .filter(e -> usuarioCalificadoDe(e)
                        .map(prestadorId::equals)
                        .orElse(false))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Calificacion> buscarPorSolicitanteId(UUID solicitanteId) {
        return calificacionRepo.findAll().stream()
                .filter(e -> usuarioCalificadorDe(e)
                        .map(solicitanteId::equals)
                        .orElse(false))
                .map(this::toDomain)
                .toList();
    }

    private CalificacionJpaEntity toEntity(Calificacion c) {
        CalificacionJpaEntity e = new CalificacionJpaEntity();
        if (c.getId() != null) {
            calificacionRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(c.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setAsignacionId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getAsignacionServicioId()));
        e.setCalificadorId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getCalificadorId()));
        e.setCalificadoId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getCalificadoId()));
        e.setRolCalificador(c.getRolCalificador() != null ? c.getRolCalificador().name().toLowerCase() : null);
        e.setPuntaje(c.getPuntaje());
        return e;
    }

    private Calificacion toDomain(CalificacionJpaEntity e) {
        // Resolvemos solicitante y prestador via asignacion
        UUID solicitudId = null;
        UUID solicitanteId = null;
        UUID prestadorId = null;
        var asignacion = asignacionRepo.findById(e.getAsignacionId());
        if (asignacion.isPresent()) {
            solicitudId = UsuarioJpaAdapter.uuidFromLong(asignacion.get().getSolicitudId());
            prestadorId = UsuarioJpaAdapter.uuidFromLong(asignacion.get().getPrestadorId());
            var solicitud = solicitudRepo.findById(asignacion.get().getSolicitudId());
            if (solicitud.isPresent()) {
                solicitanteId = UsuarioJpaAdapter.uuidFromLong(solicitud.get().getSolicitanteId());
            }
        }
        UUID calificadorId = e.getCalificadorId() != null
                ? UsuarioJpaAdapter.uuidFromLong(e.getCalificadorId())
                : solicitanteId;
        UUID calificadoId = e.getCalificadoId() != null
                ? UsuarioJpaAdapter.uuidFromLong(e.getCalificadoId())
                : prestadorId;
        RolConfirmante rolCalificador = e.getRolCalificador() != null
                ? RolConfirmante.valueOf(e.getRolCalificador().toUpperCase())
                : RolConfirmante.SOLICITANTE;
        Calificacion c = new Calificacion(
                UsuarioJpaAdapter.uuidFromLong(e.getId()), solicitudId,
                UsuarioJpaAdapter.uuidFromLong(e.getAsignacionId()),
                solicitanteId, prestadorId, calificadorId, calificadoId,
                rolCalificador, e.getPuntaje(), e.getCreatedAt());
        if (e.getCreatedAt() != null) c.marcarCreacion(e.getCreatedAt());
        return c;
    }

    private Optional<UUID> usuarioCalificadorDe(CalificacionJpaEntity e) {
        if (e.getCalificadorId() != null) {
            return Optional.of(UsuarioJpaAdapter.uuidFromLong(e.getCalificadorId()));
        }
        return asignacionRepo.findById(e.getAsignacionId())
                .flatMap(a -> solicitudRepo.findById(a.getSolicitudId()))
                .map(s -> UsuarioJpaAdapter.uuidFromLong(s.getSolicitanteId()));
    }

    private Optional<UUID> usuarioCalificadoDe(CalificacionJpaEntity e) {
        if (e.getCalificadoId() != null) {
            return Optional.of(UsuarioJpaAdapter.uuidFromLong(e.getCalificadoId()));
        }
        return asignacionRepo.findById(e.getAsignacionId())
                .map(a -> UsuarioJpaAdapter.uuidFromLong(a.getPrestadorId()));
    }
}

// ── ContraofertaJpaAdapterImpl ───────────────────────────────
@Component
class ContraofertaJpaAdapterImpl implements ContraofertaRepositoryPort {

    private final ContraofertaJpaRepository contraofertaRepo;
    private final DistribucionSolicitudJpaRepository distribucionRepo;

    ContraofertaJpaAdapterImpl(ContraofertaJpaRepository contraofertaRepo,
                                DistribucionSolicitudJpaRepository distribucionRepo) {
        this.contraofertaRepo = contraofertaRepo;
        this.distribucionRepo = distribucionRepo;
    }

    @Override
    public Contraoferta guardar(Contraoferta c) {
        ContraofertaJpaEntity e = toEntity(c);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return toDomain(contraofertaRepo.save(e));
    }

    @Override
    public Optional<Contraoferta> buscarPorId(UUID contraofertaId) {
        return contraofertaRepo.findById(contraofertaId).map(this::toDomain);
    }

    @Override
    public List<Contraoferta> buscarPorDistribucionSolicitudId(UUID distribucionId) {
        return distribucionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(distribucionId))
                .findFirst()
                .map(e -> contraofertaRepo.findByDistribucionSolicitudId(e.getId()).stream().map(this::toDomain).toList())
                .orElse(List.of());
    }

    @Override
    public Optional<Contraoferta> buscarPendientePorDistribucionSolicitudId(UUID distribucionId) {
        return distribucionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(distribucionId))
                .findFirst()
                .flatMap(e -> contraofertaRepo.findByDistribucionSolicitudIdAndEstado(e.getId(), "pendiente"))
                .map(this::toDomain);
    }

    @Override
    public List<Contraoferta> buscarPorPrestadorId(UUID prestadorId) {
        return contraofertaRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()).equals(prestadorId))
                .map(this::toDomain).toList();
    }

    private ContraofertaJpaEntity toEntity(Contraoferta c) {
        ContraofertaJpaEntity e = new ContraofertaJpaEntity();
        e.setId(c.getId());
        e.setDistribucionSolicitudId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getDistribucionSolicitudId()));
        e.setPrestadorId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getPrestadorId()));
        e.setPrecioOriginal(c.getPrecioOriginal());
        e.setPrecioPropuesto(c.getPrecioPropuesto());
        e.setMensaje(c.getMensaje());
        e.setEstado(c.getEstado() != null ? c.getEstado().name().toLowerCase() : null);
        e.setFechaEmision(c.getFechaEmision());
        e.setFechaResolucion(c.getFechaResolucion());
        return e;
    }

    private Contraoferta toDomain(ContraofertaJpaEntity e) {
        Contraoferta c = new Contraoferta(e.getId(),
                UsuarioJpaAdapter.uuidFromLong(e.getDistribucionSolicitudId()),
                UsuarioJpaAdapter.uuidFromLong(e.getPrestadorId()),
                e.getPrecioOriginal(), e.getPrecioPropuesto(), e.getMensaje(),
                EstadoContraoferta.valueOf(e.getEstado().toUpperCase()),
                e.getFechaEmision(), e.getFechaResolucion());
        if (e.getCreatedAt() != null) c.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) c.marcarModificacion(e.getUpdatedAt());
        return c;
    }
}

// ── SolicitudJpaAdapter (ConfirmacionFinalizacion) ───────────
@Component
public class SolicitudJpaAdapter implements ConfirmacionFinalizacionRepositoryPort {

    private final ConfirmacionFinalizacionJpaRepository confirmacionRepo;
    private final SolicitudServicioJpaRepository solicitudRepo;
    private final AsignacionServicioJpaRepository asignacionRepo;

    public SolicitudJpaAdapter(ConfirmacionFinalizacionJpaRepository confirmacionRepo,
                                SolicitudServicioJpaRepository solicitudRepo,
                                AsignacionServicioJpaRepository asignacionRepo) {
        this.confirmacionRepo = confirmacionRepo;
        this.solicitudRepo = solicitudRepo;
        this.asignacionRepo = asignacionRepo;
    }

    @Override
    public ConfirmacionFinalizacion guardar(ConfirmacionFinalizacion c) {
        ConfirmacionFinalizacionJpaEntity e = toEntity(c);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return toDomain(confirmacionRepo.save(e));
    }

    @Override
    public Optional<ConfirmacionFinalizacion> buscarPorId(UUID confirmacionId) {
        return confirmacionRepo.findById(confirmacionId).map(this::toDomain);
    }

    @Override
    public List<ConfirmacionFinalizacion> buscarPorSolicitudId(UUID solicitudId) {
        return solicitudRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(solicitudId))
                .findFirst()
                .map(e -> confirmacionRepo.findBySolicitudId(e.getId()).stream().map(this::toDomain).toList())
                .orElse(List.of());
    }

    @Override
    public List<ConfirmacionFinalizacion> buscarPorAsignacionServicioId(UUID asignacionId) {
        return asignacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(asignacionId))
                .findFirst()
                .map(e -> confirmacionRepo.findByAsignacionServicioId(e.getId()).stream().map(this::toDomain).toList())
                .orElse(List.of());
    }

    @Override
    public Optional<ConfirmacionFinalizacion> buscarPorAsignacionServicioIdYRolConfirmante(
            UUID asignacionId, RolConfirmante rolConfirmante) {
        return asignacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(asignacionId))
                .findFirst()
                .flatMap(e -> confirmacionRepo.findByAsignacionServicioIdAndRolConfirmante(
                        e.getId(), rolConfirmante.name().toLowerCase()))
                .map(this::toDomain);
    }

    @Override
    public List<ConfirmacionFinalizacion> buscarPorConfirmanteId(UUID confirmanteId) {
        return confirmacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getConfirmanteId()).equals(confirmanteId))
                .map(this::toDomain).toList();
    }

    private ConfirmacionFinalizacionJpaEntity toEntity(ConfirmacionFinalizacion c) {
        ConfirmacionFinalizacionJpaEntity e = new ConfirmacionFinalizacionJpaEntity();
        e.setId(c.getId());
        e.setSolicitudId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getSolicitudId()));
        e.setAsignacionServicioId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getAsignacionServicioId()));
        e.setConfirmanteId(SolicitudServicioJpaAdapterImpl.longFromUuid(c.getConfirmanteId()));
        e.setRolConfirmante(c.getRolConfirmante() != null ? c.getRolConfirmante().name().toLowerCase() : null);
        e.setConfirmada(c.getConfirmada());
        e.setFechaConfirmacion(c.getFechaConfirmacion());
        e.setObservacion(c.getObservacion());
        return e;
    }

    private ConfirmacionFinalizacion toDomain(ConfirmacionFinalizacionJpaEntity e) {
        ConfirmacionFinalizacion c = new ConfirmacionFinalizacion(
                e.getId(),
                UsuarioJpaAdapter.uuidFromLong(e.getSolicitudId()),
                UsuarioJpaAdapter.uuidFromLong(e.getAsignacionServicioId()),
                UsuarioJpaAdapter.uuidFromLong(e.getConfirmanteId()),
                RolConfirmante.valueOf(e.getRolConfirmante().toUpperCase()),
                e.getConfirmada(), e.getFechaConfirmacion(), e.getObservacion());
        if (e.getCreatedAt() != null) c.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) c.marcarModificacion(e.getUpdatedAt());
        return c;
    }
}
