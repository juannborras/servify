package com.servify.chat.infrastructure.persistence;

import com.servify.chat.application.port.out.MensajeChatRepositoryPort;
import com.servify.chat.domain.model.MensajeChat;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_mensaje")
class MensajeChatJpaEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;
    @Column(name = "prestador_id", nullable = false)
    private Long prestadorId;
    @Column(name = "remitente_id", nullable = false)
    private Long remitenteId;
    @Column(name = "contenido", nullable = false, length = 1200)
    private String contenido;
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected MensajeChatJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }
    public Long getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(Long solicitanteId) { this.solicitanteId = solicitanteId; }
    public Long getPrestadorId() { return prestadorId; }
    public void setPrestadorId(Long prestadorId) { this.prestadorId = prestadorId; }
    public Long getRemitenteId() { return remitenteId; }
    public void setRemitenteId(Long remitenteId) { this.remitenteId = remitenteId; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

interface MensajeChatJpaRepository extends JpaRepository<MensajeChatJpaEntity, UUID> {

    List<MensajeChatJpaEntity> findBySolicitudIdAndPrestadorIdOrderByFechaEnvioAsc(Long solicitudId, Long prestadorId);
}

@Component
public class MensajeChatJpaAdapter implements MensajeChatRepositoryPort {

    private final MensajeChatJpaRepository mensajeChatRepo;

    public MensajeChatJpaAdapter(MensajeChatJpaRepository mensajeChatRepo) {
        this.mensajeChatRepo = mensajeChatRepo;
    }

    @Override
    public MensajeChat guardar(MensajeChat mensaje) {
        MensajeChatJpaEntity entity = toEntity(mensaje);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(mensajeChatRepo.save(entity));
    }

    @Override
    public List<MensajeChat> listarPorSolicitudYPrestador(UUID solicitudId, UUID prestadorId) {
        return mensajeChatRepo
                .findBySolicitudIdAndPrestadorIdOrderByFechaEnvioAsc(longFromUuid(solicitudId), longFromUuid(prestadorId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MensajeChatJpaEntity toEntity(MensajeChat mensaje) {
        MensajeChatJpaEntity entity = new MensajeChatJpaEntity();
        entity.setId(mensaje.getId());
        entity.setSolicitudId(longFromUuid(mensaje.getSolicitudId()));
        entity.setSolicitanteId(longFromUuid(mensaje.getSolicitanteId()));
        entity.setPrestadorId(longFromUuid(mensaje.getPrestadorId()));
        entity.setRemitenteId(longFromUuid(mensaje.getRemitenteId()));
        entity.setContenido(mensaje.getContenido());
        entity.setFechaEnvio(mensaje.getFechaEnvio());
        return entity;
    }

    private MensajeChat toDomain(MensajeChatJpaEntity entity) {
        MensajeChat mensaje = new MensajeChat(
                entity.getId(),
                UsuarioJpaAdapter.uuidFromLong(entity.getSolicitudId()),
                UsuarioJpaAdapter.uuidFromLong(entity.getSolicitanteId()),
                UsuarioJpaAdapter.uuidFromLong(entity.getPrestadorId()),
                UsuarioJpaAdapter.uuidFromLong(entity.getRemitenteId()),
                entity.getContenido(),
                entity.getFechaEnvio()
        );
        if (entity.getCreatedAt() != null) {
            mensaje.marcarCreacion(entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() != null) {
            mensaje.marcarModificacion(entity.getUpdatedAt());
        }
        return mensaje;
    }

    private Long longFromUuid(UUID uuid) {
        return uuid == null ? null : uuid.getLeastSignificantBits();
    }
}
