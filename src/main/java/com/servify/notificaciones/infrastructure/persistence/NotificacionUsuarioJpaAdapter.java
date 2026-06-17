package com.servify.notificaciones.infrastructure.persistence;

import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;
import com.servify.notificaciones.domain.enumtype.TipoNotificacion;
import com.servify.notificaciones.domain.model.NotificacionUsuario;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "notificacion_usuario")
class NotificacionUsuarioJpaEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    @Column(name = "tipo", nullable = false)
    private String tipo;
    @Column(name = "titulo", nullable = false)
    private String titulo;
    @Column(name = "mensaje", nullable = false)
    private String mensaje;
    @Column(name = "referencia_tipo")
    private String referenciaTipo;
    @Column(name = "referencia_id", columnDefinition = "uuid")
    private UUID referenciaId;
    @Column(name = "leida", nullable = false)
    private Boolean leida;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NotificacionUsuarioJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }
    public UUID getReferenciaId() { return referenciaId; }
    public void setReferenciaId(UUID referenciaId) { this.referenciaId = referenciaId; }
    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDateTime fechaLectura) { this.fechaLectura = fechaLectura; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

interface NotificacionUsuarioJpaRepository extends JpaRepository<NotificacionUsuarioJpaEntity, UUID> {

    List<NotificacionUsuarioJpaEntity> findByUsuarioId(Long usuarioId);
}

@Component
public class NotificacionUsuarioJpaAdapter implements NotificacionUsuarioRepositoryPort {

    private final NotificacionUsuarioJpaRepository notificacionRepo;

    public NotificacionUsuarioJpaAdapter(NotificacionUsuarioJpaRepository notificacionRepo) {
        this.notificacionRepo = notificacionRepo;
    }

    @Override
    public NotificacionUsuario guardar(NotificacionUsuario notificacion) {
        NotificacionUsuarioJpaEntity entity = toEntity(notificacion);
        if (entity.getId() != null) {
            notificacionRepo.findById(entity.getId())
                    .ifPresent(existing -> entity.setCreatedAt(existing.getCreatedAt()));
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(notificacionRepo.save(entity));
    }

    @Override
    public Optional<NotificacionUsuario> buscarPorId(UUID notificacionId) {
        return notificacionRepo.findById(notificacionId).map(this::toDomain);
    }

    @Override
    public List<NotificacionUsuario> buscarPorUsuarioId(UUID usuarioId) {
        return notificacionRepo.findByUsuarioId(usuarioId.getLeastSignificantBits()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminarPorId(UUID notificacionId) {
        notificacionRepo.deleteById(notificacionId);
    }

    private NotificacionUsuarioJpaEntity toEntity(NotificacionUsuario notificacion) {
        NotificacionUsuarioJpaEntity entity = new NotificacionUsuarioJpaEntity();
        entity.setId(notificacion.getId());
        entity.setUsuarioId(notificacion.getUsuarioId() != null ? notificacion.getUsuarioId().getLeastSignificantBits() : null);
        entity.setTipo(notificacion.getTipo() != null ? notificacion.getTipo().name().toLowerCase() : null);
        entity.setTitulo(notificacion.getTitulo());
        entity.setMensaje(notificacion.getMensaje());
        entity.setReferenciaTipo(notificacion.getReferenciaTipo());
        entity.setReferenciaId(notificacion.getReferenciaId());
        entity.setLeida(notificacion.getLeida());
        entity.setFechaCreacion(notificacion.getFechaCreacion());
        entity.setFechaLectura(notificacion.getFechaLectura());
        return entity;
    }

    private NotificacionUsuario toDomain(NotificacionUsuarioJpaEntity entity) {
        NotificacionUsuario notificacion = new NotificacionUsuario(
                entity.getId(),
                UsuarioJpaAdapter.uuidFromLong(entity.getUsuarioId()),
                TipoNotificacion.valueOf(entity.getTipo().toUpperCase()),
                entity.getTitulo(),
                entity.getMensaje(),
                entity.getReferenciaTipo(),
                entity.getReferenciaId(),
                entity.getLeida(),
                entity.getFechaCreacion(),
                entity.getFechaLectura()
        );
        if (entity.getCreatedAt() != null) {
            notificacion.marcarCreacion(entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() != null) {
            notificacion.marcarModificacion(entity.getUpdatedAt());
        }
        return notificacion;
    }
}
