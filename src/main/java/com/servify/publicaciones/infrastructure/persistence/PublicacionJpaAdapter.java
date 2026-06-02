package com.servify.publicaciones.infrastructure.persistence;

import com.servify.administracion.application.port.out.PublicacionModerablePort;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.enumtype.EstadoCategoria;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ── CategoriaServicioJpaAdapter ──────────────────────────────
@Component
class CategoriaServicioJpaAdapter implements CategoriaServicioRepositoryPort {

    private final CategoriaServicioJpaRepository categoriaRepo;

    CategoriaServicioJpaAdapter(CategoriaServicioJpaRepository categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public CategoriaServicio guardar(CategoriaServicio categoria) {
        CategoriaServicioJpaEntity e = toCategoriaEntity(categoria);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        return toCategoriaDomain(categoriaRepo.save(e));
    }

    @Override
    public Optional<CategoriaServicio> buscarPorId(UUID categoriaId) {
        return categoriaRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(categoriaId))
                .findFirst().map(this::toCategoriaDomain);
    }

    @Override
    public Optional<CategoriaServicio> buscarPorNombre(String nombre) {
        return categoriaRepo.findByNombreIgnoreCase(nombre).map(this::toCategoriaDomain);
    }

    @Override
    public List<CategoriaServicio> listarActivas() {
        return categoriaRepo.findByActivaTrue().stream().map(this::toCategoriaDomain).toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return categoriaRepo.existsByNombreIgnoreCase(nombre);
    }

    CategoriaServicioJpaEntity toCategoriaEntity(CategoriaServicio c) {
        CategoriaServicioJpaEntity e = new CategoriaServicioJpaEntity();
        if (c.getId() != null) {
            categoriaRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(c.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setNombre(c.getNombre());
        e.setDescripcion(c.getDescripcion());
        e.setActiva(c.estaActiva());
        return e;
    }

    CategoriaServicio toCategoriaDomain(CategoriaServicioJpaEntity e) {
        CategoriaServicio c = new CategoriaServicio(
                UsuarioJpaAdapter.uuidFromLong(e.getId()),
                e.getNombre(), e.getDescripcion(),
                Boolean.TRUE.equals(e.getActiva()) ? EstadoCategoria.ACTIVA : EstadoCategoria.INACTIVA);
        if (e.getCreatedAt() != null) c.marcarCreacion(e.getCreatedAt());
        return c;
    }
}

// ── PublicacionJpaAdapter ────────────────────────────────────
@Component
public class PublicacionJpaAdapter implements PublicacionServicioRepositoryPort, PublicacionModerablePort {

    private final CategoriaServicioJpaRepository categoriaRepo;
    private final PublicacionServicioJpaRepository publicacionRepo;
    private final DisponibilidadHorariaJpaRepository disponibilidadRepo;
    private final PublicacionZonaCoberturaJpaRepository zonaCoberturaRepo;
    private final CategoriaServicioJpaAdapter categoriaAdapter;

    public PublicacionJpaAdapter(CategoriaServicioJpaRepository categoriaRepo,
                                  PublicacionServicioJpaRepository publicacionRepo,
                                  DisponibilidadHorariaJpaRepository disponibilidadRepo,
                                  PublicacionZonaCoberturaJpaRepository zonaCoberturaRepo,
                                  CategoriaServicioJpaAdapter categoriaAdapter) {
        this.categoriaRepo = categoriaRepo;
        this.publicacionRepo = publicacionRepo;
        this.disponibilidadRepo = disponibilidadRepo;
        this.zonaCoberturaRepo = zonaCoberturaRepo;
        this.categoriaAdapter = categoriaAdapter;
    }

    @Override
    @Transactional
    public PublicacionServicio guardar(PublicacionServicio publicacion) {
        PublicacionServicioJpaEntity e = toPublicacionEntity(publicacion);
        if (e.getCreatedAt() == null) e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        PublicacionServicioJpaEntity saved = publicacionRepo.save(e);
        disponibilidadRepo.deleteByPublicacionId(saved.getId());
        if (publicacion.getDisponibilidadesHorarias() != null) {
            publicacion.getDisponibilidadesHorarias().forEach(d -> {
                DisponibilidadHorariaJpaEntity de = new DisponibilidadHorariaJpaEntity();
                de.setPublicacionId(saved.getId());
                de.setDiaSemana(d.getDiaSemana().name().toLowerCase());
                de.setHoraInicio(d.getHoraDesde());
                de.setHoraFin(d.getHoraHasta());
                disponibilidadRepo.save(de);
            });
        }
        zonaCoberturaRepo.deleteByPublicacionId(saved.getId());
        List<Ubicacion> zonas = publicacion.getZonasCobertura();
        if (zonas == null || zonas.isEmpty()) {
            zonas = publicacion.getUbicacion() == null ? List.of() : List.of(publicacion.getUbicacion());
        }
        zonas.forEach(zona -> {
            PublicacionZonaCoberturaJpaEntity ze = toZonaEntity(saved.getId(), zona);
            zonaCoberturaRepo.save(ze);
        });
        return toPublicacionDomain(saved);
    }

    @Override
    public Optional<PublicacionServicio> buscarPorId(UUID publicacionId) {
        return publicacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(publicacionId))
                .findFirst().map(this::toPublicacionDomain);
    }

    @Override
    public List<PublicacionServicio> buscarPorUsuarioId(UUID usuarioId) {
        return publicacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getUsuarioId()).equals(usuarioId))
                .map(this::toPublicacionDomain).toList();
    }

    @Override
    public List<PublicacionServicio> buscarActivas() {
        return publicacionRepo.findByEstado("activa").stream().map(this::toPublicacionDomain).toList();
    }

    @Override
    public List<PublicacionServicio> buscarActivasPorCategoria(UUID categoriaId) {
        return publicacionRepo.findAll().stream()
                .filter(e -> "activa".equals(e.getEstado())
                        && UsuarioJpaAdapter.uuidFromLong(e.getCategoriaId()).equals(categoriaId))
                .map(this::toPublicacionDomain).toList();
    }

    @Override
    public boolean existePorUsuarioIdYTitulo(UUID usuarioId, String titulo) {
        return publicacionRepo.findAll().stream()
                .anyMatch(e -> UsuarioJpaAdapter.uuidFromLong(e.getUsuarioId()).equals(usuarioId)
                        && !"eliminada".equalsIgnoreCase(e.getEstado())
                        && e.getTitulo() != null && e.getTitulo().equalsIgnoreCase(titulo));
    }

    @Override
    public boolean existePorUsuarioIdTituloYLocalidad(UUID usuarioId, String titulo, String localidad) {
        return publicacionRepo.findAll().stream()
                .anyMatch(e -> UsuarioJpaAdapter.uuidFromLong(e.getUsuarioId()).equals(usuarioId)
                        && !"eliminada".equalsIgnoreCase(e.getEstado())
                        && e.getTitulo() != null && e.getTitulo().equalsIgnoreCase(titulo)
                        && mismaLocalidad(e.getLocalidad(), localidad));
    }

    @Override
    public boolean existePublicacion(UUID publicacionId) {
        return buscarPorId(publicacionId).isPresent();
    }

    @Override
    @Transactional
    public void moderarPublicacion(UUID publicacionId, String estadoDestino, String motivo) {
        publicacionRepo.findAll().stream()
                .filter(e -> UsuarioJpaAdapter.uuidFromLong(e.getId()).equals(publicacionId))
                .findFirst().ifPresent(e -> {
                    e.setEstado(estadoDestino.trim().toLowerCase());
                    e.setUpdatedAt(LocalDateTime.now());
                    publicacionRepo.save(e);
                });
    }

    private PublicacionServicioJpaEntity toPublicacionEntity(PublicacionServicio p) {
        PublicacionServicioJpaEntity e = new PublicacionServicioJpaEntity();
        if (p.getId() != null) {
            publicacionRepo.findAll().stream()
                    .filter(ex -> UsuarioJpaAdapter.uuidFromLong(ex.getId()).equals(p.getId()))
                    .findFirst().ifPresent(ex -> {
                        e.setId(ex.getId());
                        e.setCreatedAt(ex.getCreatedAt());
                    });
        }
        e.setUsuarioId(longFromUuid(p.getUsuarioId()));
        if (p.getCategoriaServicio() != null) e.setCategoriaId(longFromUuid(p.getCategoriaServicio().getId()));
        e.setTitulo(p.getTitulo());
        e.setDescripcion(p.getDescripcion());
        e.setModalidad(modalidadToDb(p.getModalidadServicio()));
        if (p.getUbicacion() != null) {
            e.setPais(p.getUbicacion().getPais()); e.setProvincia(p.getUbicacion().getProvincia());
            e.setCiudad(p.getUbicacion().getCiudad()); e.setLocalidad(p.getUbicacion().getLocalidad());
            e.setCalle(p.getUbicacion().getCalle()); e.setAltura(p.getUbicacion().getAltura());
            e.setReferencia(p.getUbicacion().getReferencia());
            e.setLatitud(p.getUbicacion().getLatitud()); e.setLongitud(p.getUbicacion().getLongitud());
        }
        e.setPrecioBase(p.getPrecioBase());
        e.setEstado(p.getEstado() != null ? p.getEstado().name().toLowerCase() : "activa");
        return e;
    }

    private PublicacionServicio toPublicacionDomain(PublicacionServicioJpaEntity e) {
        CategoriaServicio categoria = categoriaRepo.findById(e.getCategoriaId())
                .map(categoriaAdapter::toCategoriaDomain).orElse(null);
        List<DisponibilidadHoraria> disponibilidades = disponibilidadRepo
                .findByPublicacionId(e.getId()).stream()
                .map(d -> new DisponibilidadHoraria(
                        DayOfWeek.valueOf(d.getDiaSemana().toUpperCase()),
                        d.getHoraInicio(), d.getHoraFin())).toList();
        Ubicacion ubicacion = new Ubicacion(
                e.getPais(), e.getProvincia(), e.getCiudad(), e.getLocalidad(),
                e.getCalle(), e.getAltura(), e.getReferencia(), e.getLatitud(), e.getLongitud());
        List<Ubicacion> zonas = zonaCoberturaRepo.findByPublicacionId(e.getId()).stream()
                .map(this::toUbicacion)
                .toList();
        if (zonas.isEmpty()) {
            zonas = List.of(ubicacion);
        }
        PublicacionServicio p = new PublicacionServicio(
                UsuarioJpaAdapter.uuidFromLong(e.getId()),
                UsuarioJpaAdapter.uuidFromLong(e.getUsuarioId()),
                categoria, e.getTitulo(), e.getDescripcion(),
                modalidadFromDb(e.getModalidad()), ubicacion, zonas, disponibilidades,
                e.getPrecioBase(), EstadoPublicacion.valueOf(e.getEstado().toUpperCase()));
        if (e.getCreatedAt() != null) p.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) p.marcarModificacion(e.getUpdatedAt());
        return p;
    }

    private PublicacionZonaCoberturaJpaEntity toZonaEntity(Long publicacionId, Ubicacion zona) {
        PublicacionZonaCoberturaJpaEntity e = new PublicacionZonaCoberturaJpaEntity();
        e.setPublicacionId(publicacionId);
        e.setPais(zona.getPais());
        e.setProvincia(zona.getProvincia());
        e.setCiudad(zona.getCiudad());
        e.setLocalidad(zona.getLocalidad());
        e.setCalle(zona.getCalle());
        e.setAltura(zona.getAltura());
        e.setReferencia(zona.getReferencia());
        e.setLatitud(zona.getLatitud());
        e.setLongitud(zona.getLongitud());
        return e;
    }

    private Ubicacion toUbicacion(PublicacionZonaCoberturaJpaEntity e) {
        return new Ubicacion(
                e.getPais(), e.getProvincia(), e.getCiudad(), e.getLocalidad(),
                e.getCalle(), e.getAltura(), e.getReferencia(), e.getLatitud(), e.getLongitud());
    }

    private String modalidadToDb(ModalidadServicio m) {
        if (m == null) return null;
        return switch (m) {
            case PRESENCIAL -> "presencial";
            case VIRTUAL -> "virtual";
            case MIXTA -> "mixta";
        };
    }

    private ModalidadServicio modalidadFromDb(String s) {
        if (s == null) return null;
        return switch (s.toLowerCase()) {
            case "presencial" -> ModalidadServicio.PRESENCIAL;
            case "virtual" -> ModalidadServicio.VIRTUAL;
            case "mixta", "ambas" -> ModalidadServicio.MIXTA;
            default -> throw new IllegalArgumentException("Modalidad desconocida: " + s);
        };
    }

    private Long longFromUuid(UUID uuid) {
        if (uuid == null) return null;
        return uuid.getLeastSignificantBits();
    }

    private boolean mismaLocalidad(String actual, String requerida) {
        if (actual == null || actual.isBlank()) {
            return requerida == null || requerida.isBlank();
        }
        return requerida != null && actual.equalsIgnoreCase(requerida);
    }
}
