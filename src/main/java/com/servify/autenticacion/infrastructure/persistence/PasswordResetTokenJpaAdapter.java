package com.servify.autenticacion.infrastructure.persistence;

import com.servify.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.servify.autenticacion.domain.model.PasswordResetToken;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "password_reset_token")
class PasswordResetTokenJpaEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    @Column(name = "credencial_acceso_id", nullable = false, columnDefinition = "uuid")
    private UUID credencialAccesoId;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;
    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;
    @Column(name = "utilizado", nullable = false)
    private Boolean utilizado;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PasswordResetTokenJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public UUID getCredencialAccesoId() { return credencialAccesoId; }
    public void setCredencialAccesoId(UUID credencialAccesoId) { this.credencialAccesoId = credencialAccesoId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public LocalDateTime getFechaUso() { return fechaUso; }
    public void setFechaUso(LocalDateTime fechaUso) { this.fechaUso = fechaUso; }
    public Boolean getUtilizado() { return utilizado; }
    public void setUtilizado(Boolean utilizado) { this.utilizado = utilizado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    List<PasswordResetTokenJpaEntity> findByUsuarioIdAndUtilizadoFalseAndFechaExpiracionAfter(
            Long usuarioId,
            LocalDateTime ahora
    );
}

@Component
public class PasswordResetTokenJpaAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository repository;

    public PasswordResetTokenJpaAdapter(PasswordResetTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PasswordResetToken guardar(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = toEntity(token);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<PasswordResetToken> buscarPorTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public List<PasswordResetToken> buscarVigentesPorUsuarioId(UUID usuarioId, LocalDateTime ahora) {
        return repository.findByUsuarioIdAndUtilizadoFalseAndFechaExpiracionAfter(
                        usuarioId != null ? usuarioId.getLeastSignificantBits() : null,
                        ahora != null ? ahora : LocalDateTime.now()
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PasswordResetTokenJpaEntity toEntity(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUsuarioId(token.getUsuarioId() != null ? token.getUsuarioId().getLeastSignificantBits() : null);
        entity.setCredencialAccesoId(token.getCredencialAccesoId());
        entity.setEmail(token.getEmail());
        entity.setTokenHash(token.getTokenHash());
        entity.setFechaCreacion(token.getFechaCreacion());
        entity.setFechaExpiracion(token.getFechaExpiracion());
        entity.setFechaUso(token.getFechaUso());
        entity.setUtilizado(Boolean.TRUE.equals(token.getUtilizado()));
        return entity;
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        PasswordResetToken token = new PasswordResetToken(
                entity.getId(),
                UsuarioJpaAdapter.uuidFromLong(entity.getUsuarioId()),
                entity.getCredencialAccesoId(),
                entity.getEmail(),
                entity.getTokenHash(),
                entity.getFechaCreacion(),
                entity.getFechaExpiracion(),
                entity.getFechaUso(),
                entity.getUtilizado()
        );
        if (entity.getCreatedAt() != null) token.marcarCreacion(entity.getCreatedAt());
        if (entity.getUpdatedAt() != null) token.marcarModificacion(entity.getUpdatedAt());
        return token;
    }
}
