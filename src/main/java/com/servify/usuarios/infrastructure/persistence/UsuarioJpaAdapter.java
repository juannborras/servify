package com.servify.usuarios.infrastructure.persistence;

import com.servify.administracion.application.port.out.UsuarioAdministrablePort;
import com.servify.administracion.domain.enumtype.TipoMedida;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.publicaciones.application.port.out.UsuarioPublicadorPort;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.EstadoValidacionIdentidad;
import com.servify.usuarios.domain.enumtype.Rol;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.Contacto;
import com.servify.usuarios.domain.valueobject.NombreCompleto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ── PerfilUsuarioJpaAdapterImpl ──────────────────────────────
@Component
class PerfilUsuarioJpaAdapterImpl implements PerfilUsuarioRepositoryPort {

    private final PerfilUsuarioJpaRepository perfilRepo;
    private final UsuarioJpaRepository usuarioRepo;

    PerfilUsuarioJpaAdapterImpl(PerfilUsuarioJpaRepository perfilRepo,
                                 UsuarioJpaRepository usuarioRepo) {
        this.perfilRepo = perfilRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public PerfilUsuario guardar(PerfilUsuario perfil) {
        PerfilUsuarioJpaEntity entity = toEntity(perfil);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(perfilRepo.save(entity));
    }

    @Override
    public Optional<PerfilUsuario> buscarPorId(UUID perfilId) {
        return perfilRepo.findAll().stream()
                .filter(e -> uuidFromLong(e.getId()).equals(perfilId))
                .findFirst().map(this::toDomain);
    }

    @Override
    public Optional<PerfilUsuario> buscarPorUsuarioId(UUID usuarioId) {
        return usuarioRepo.findAll().stream()
                .filter(e -> uuidFromLong(e.getId()).equals(usuarioId))
                .findFirst()
                .flatMap(e -> perfilRepo.findByUsuarioId(e.getId()))
                .map(this::toDomain);
    }

    PerfilUsuarioJpaEntity toEntity(PerfilUsuario p) {
        PerfilUsuarioJpaEntity e = new PerfilUsuarioJpaEntity();
        if (p.getId() != null) {
            perfilRepo.findAll().stream()
                    .filter(ex -> uuidFromLong(ex.getId()).equals(p.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        usuarioRepo.findAll().stream()
                .filter(ex -> uuidFromLong(ex.getId()).equals(p.getUsuarioId()))
                .findFirst().ifPresent(ex -> e.setUsuarioId(ex.getId()));
        if (p.getNombreCompleto() != null) {
            e.setNombre(p.getNombreCompleto().getNombre());
            e.setApellido(p.getNombreCompleto().getApellido());
        }
        e.setEdad(p.getEdad());
        e.setFotoPerfilUrl(p.getFotoPerfilUrl());
        if (p.getUbicacion() != null) {
            Ubicacion ub = p.getUbicacion();
            e.setPais(ub.getPais()); e.setProvincia(ub.getProvincia());
            e.setCiudad(ub.getCiudad()); e.setLocalidad(ub.getLocalidad());
            e.setCalle(ub.getCalle()); e.setAltura(ub.getAltura());
            e.setReferencia(ub.getReferencia());
            e.setLatitud(ub.getLatitud()); e.setLongitud(ub.getLongitud());
        }
        e.setDescripcionPersonal(p.getDescripcionPersonal());
        e.setPerfilCompleto(p.getPerfilCompleto());
        return e;
    }

    PerfilUsuario toDomain(PerfilUsuarioJpaEntity e) {
        Ubicacion ubicacion = new Ubicacion(
                e.getPais(), e.getProvincia(), e.getCiudad(), e.getLocalidad(),
                e.getCalle(), e.getAltura(), e.getReferencia(), e.getLatitud(), e.getLongitud());
        PerfilUsuario p = new PerfilUsuario(
                uuidFromLong(e.getId()), uuidFromLong(e.getUsuarioId()),
                new NombreCompleto(e.getNombre(), e.getApellido()),
                e.getEdad(), e.getFotoPerfilUrl(), ubicacion,
                e.getDescripcionPersonal(), e.getPerfilCompleto());
        if (e.getCreatedAt() != null) p.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) p.marcarModificacion(e.getUpdatedAt());
        return p;
    }

    static UUID uuidFromLong(Long id) {
        if (id == null) return null;
        return new UUID(0L, id);
    }
}

// ── UsuarioJpaAdapter ────────────────────────────────────────
@Component
public class UsuarioJpaAdapter implements UsuarioRepositoryPort,
        UsuarioPublicadorPort, UsuarioAutenticablePort, UsuarioAdministrablePort {

    private final UsuarioJpaRepository usuarioRepo;
    private final PerfilUsuarioJpaRepository perfilRepo;
    private final PerfilUsuarioJpaAdapterImpl perfilAdapter;

    public UsuarioJpaAdapter(UsuarioJpaRepository usuarioRepo,
                              PerfilUsuarioJpaRepository perfilRepo,
                              PerfilUsuarioJpaAdapterImpl perfilAdapter) {
        this.usuarioRepo = usuarioRepo;
        this.perfilRepo = perfilRepo;
        this.perfilAdapter = perfilAdapter;
    }

    // ── UsuarioRepositoryPort ────────────────────────────────

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entity = toEntity(usuario);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(usuarioRepo.save(entity), null);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID usuarioId) {
        return usuarioRepo.findAll().stream()
                .filter(e -> uuidFromLong(e.getId()).equals(usuarioId))
                .findFirst()
                .map(e -> toDomain(e, perfilRepo.findByUsuarioId(e.getId()).orElse(null)));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByEmailIgnoreCase(email)
                .map(e -> toDomain(e, perfilRepo.findByUsuarioId(e.getId()).orElse(null)));
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioRepo.existsByEmailIgnoreCase(email);
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepo.findByNombreUsuarioIgnoreCase(nombreUsuario)
                .map(e -> toDomain(e, perfilRepo.findByUsuarioId(e.getId()).orElse(null)));
    }

    @Override
    public boolean existePorNombreUsuario(String nombreUsuario) {
        return usuarioRepo.existsByNombreUsuarioIgnoreCase(nombreUsuario);
    }

    @Override
    public List<Usuario> listarPorEstado(EstadoUsuario estado) {
        return usuarioRepo.findAll().stream()
                .filter(e -> estado == null || e.getEstado().equalsIgnoreCase(estado.name()))
                .map(e -> toDomain(e, perfilRepo.findByUsuarioId(e.getId()).orElse(null)))
                .toList();
    }

    // ── UsuarioPublicadorPort ────────────────────────────────

    @Override
    public boolean existeUsuario(UUID usuarioId) {
        return buscarPorId(usuarioId).isPresent();
    }

    @Override
    public boolean puedePublicarServicios(UUID usuarioId) {
        return buscarPorId(usuarioId).map(Usuario::puedePublicarServicios).orElse(false);
    }

    // ── UsuarioAutenticablePort ──────────────────────────────

    @Override
    public boolean puedeAutenticarse(UUID usuarioId) {
        return buscarPorId(usuarioId).map(Usuario::estaActivo).orElse(false);
    }

    @Override
    public boolean coincideEmailPrincipal(UUID usuarioId, String email) {
        return buscarPorId(usuarioId)
                .map(u -> u.getContacto() != null && u.getContacto().getEmail() != null
                        && u.getContacto().getEmail().equalsIgnoreCase(email))
                .orElse(false);
    }

    @Override
    public Optional<UUID> buscarUsuarioIdPorNombreUsuario(String nombreUsuario) {
        return buscarPorNombreUsuario(nombreUsuario).map(Usuario::getId);
    }

    // ── UsuarioAdministrablePort ─────────────────────────────

    @Override
    public boolean esAdministrador(UUID usuarioId) {
        return buscarPorId(usuarioId).map(Usuario::esAdmin).orElse(false);
    }

    @Override
    public void aplicarMedida(UUID usuarioId, TipoMedida tipoMedida, String motivo) {
        buscarPorId(usuarioId).ifPresent(usuario -> {
            if (TipoMedida.BLOQUEO.equals(tipoMedida)) usuario.bloquear();
            else if (TipoMedida.SUSPENSION.equals(tipoMedida)) usuario.suspender();
            guardar(usuario);
        });
    }

    // ── Mapeos ───────────────────────────────────────────────

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        if (u.getId() != null) {
            usuarioRepo.findAll().stream()
                    .filter(ex -> uuidFromLong(ex.getId()).equals(u.getId()))
                    .findFirst().ifPresent(ex -> e.setId(ex.getId()));
        }
        e.setNombreUsuario(u.getNombreUsuario());
        e.setEmail(u.getContacto() != null ? u.getContacto().getEmail() : null);
        e.setTelefono(u.getContacto() != null ? u.getContacto().getTelefono() : null);
        e.setRol(u.getRol() != null ? u.getRol().name().toLowerCase() : null);
        e.setEstado(u.getEstado() != null ? u.getEstado().name().toLowerCase() : null);
        e.setEstadoValidacionIdentidad(u.getEstadoValidacionIdentidad() != null
                ? u.getEstadoValidacionIdentidad().name().toLowerCase() : null);
        e.setFechaRegistro(u.getFechaRegistro());
        return e;
    }

    private Usuario toDomain(UsuarioJpaEntity e, PerfilUsuarioJpaEntity perfilEntity) {
        PerfilUsuario perfil = perfilEntity != null ? perfilAdapter.toDomain(perfilEntity) : null;
        Usuario u = new Usuario(
                uuidFromLong(e.getId()),
                e.getNombreUsuario(),
                new Contacto(e.getEmail(), e.getTelefono()),
                Rol.valueOf(e.getRol().toUpperCase()),
                EstadoUsuario.valueOf(e.getEstado().toUpperCase()),
                EstadoValidacionIdentidad.valueOf(e.getEstadoValidacionIdentidad().toUpperCase()),
                perfil, e.getFechaRegistro());
        if (e.getCreatedAt() != null) u.marcarCreacion(e.getCreatedAt());
        if (e.getUpdatedAt() != null) u.marcarModificacion(e.getUpdatedAt());
        return u;
    }

    public static UUID uuidFromLong(Long id) {
        if (id == null) return null;
        return new UUID(0L, id);
    }
}
