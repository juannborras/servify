package com.servify.pagos.infrastructure.persistence;

import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.pagos.domain.enumtype.EstadoPagoServicio;
import com.servify.pagos.domain.model.PagoServicio;
import com.servify.usuarios.infrastructure.persistence.UsuarioJpaAdapter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class PagoServicioJpaAdapter implements PagoServicioRepositoryPort {
    private final PagoServicioJpaRepository repository;

    PagoServicioJpaAdapter(PagoServicioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PagoServicio guardar(PagoServicio pago) {
        PagoServicioJpaEntity entity = toEntity(pago);
        LocalDateTime ahora = LocalDateTime.now();
        if (entity.getCreatedAt() == null) entity.setCreatedAt(ahora);
        entity.setUpdatedAt(ahora);
        return toDomain(repository.save(entity));
    }

    @Override public Optional<PagoServicio> buscarPorId(UUID pagoId) {
        return repository.findById(pagoId).map(this::toDomain);
    }

    @Override public Optional<PagoServicio> buscarPorIdParaActualizar(UUID pagoId) {
        return repository.findByIdForUpdate(pagoId).map(this::toDomain);
    }

    @Override
    public Optional<PagoServicio> buscarPorObjetivo(UUID asignacionId, UUID encuentroId) {
        return (encuentroId == null
                ? repository.findByAsignacionServicioIdAndEncuentroServicioIdIsNull(longFromUuid(asignacionId))
                : repository.findByEncuentroServicioId(encuentroId)).map(this::toDomain);
    }

    @Override
    public Optional<PagoServicio> buscarPorObjetivoParaActualizar(UUID asignacionId, UUID encuentroId) {
        return (encuentroId == null
                ? repository.findAsignacionForUpdate(longFromUuid(asignacionId))
                : repository.findEncuentroForUpdate(encuentroId)).map(this::toDomain);
    }

    @Override public Optional<PagoServicio> buscarPorExternalReference(String externalReference) {
        return repository.findByExternalReference(externalReference).map(this::toDomain);
    }

    @Override public Optional<PagoServicio> buscarPorExternalReferenceParaActualizar(String externalReference) {
        return repository.findExternalReferenceForUpdate(externalReference).map(this::toDomain);
    }

    @Override public Optional<PagoServicio> buscarPorMercadoPagoPaymentId(String paymentId) {
        return repository.findByMercadoPagoPaymentId(paymentId).map(this::toDomain);
    }

    private PagoServicioJpaEntity toEntity(PagoServicio pago) {
        PagoServicioJpaEntity entity = repository.findById(pago.getId()).orElseGet(PagoServicioJpaEntity::new);
        entity.setId(pago.getId());
        entity.setSolicitudId(longFromUuid(pago.getSolicitudId()));
        entity.setAsignacionServicioId(longFromUuid(pago.getAsignacionServicioId()));
        entity.setEncuentroServicioId(pago.getEncuentroServicioId());
        entity.setSolicitanteId(longFromUuid(pago.getSolicitanteId()));
        entity.setMonto(pago.getMonto());
        entity.setMoneda(pago.getMoneda());
        entity.setEstado(pago.getEstado().name().toLowerCase());
        entity.setExternalReference(pago.getExternalReference());
        entity.setMercadoPagoPreferenceId(pago.getMercadoPagoPreferenceId());
        entity.setCheckoutUrl(pago.getCheckoutUrl());
        entity.setMercadoPagoPaymentId(pago.getMercadoPagoPaymentId());
        entity.setAprobadoEn(pago.getAprobadoEn());
        entity.setErrorDetalle(pago.getErrorDetalle());
        entity.setCreatedAt(pago.getFechaCreacion());
        entity.setUpdatedAt(pago.getFechaUltimaModificacion());
        return entity;
    }

    private PagoServicio toDomain(PagoServicioJpaEntity entity) {
        PagoServicio pago = new PagoServicio(
                entity.getId(), uuidFromLong(entity.getSolicitudId()), uuidFromLong(entity.getAsignacionServicioId()),
                entity.getEncuentroServicioId(), uuidFromLong(entity.getSolicitanteId()), entity.getMonto(),
                entity.getMoneda(), EstadoPagoServicio.valueOf(entity.getEstado().toUpperCase()),
                entity.getExternalReference(), entity.getMercadoPagoPreferenceId(), entity.getCheckoutUrl(),
                entity.getMercadoPagoPaymentId(), entity.getAprobadoEn(), entity.getErrorDetalle());
        pago.marcarCreacion(entity.getCreatedAt());
        if (entity.getUpdatedAt() != null) pago.marcarModificacion(entity.getUpdatedAt());
        return pago;
    }

    private static Long longFromUuid(UUID id) { return id == null ? null : id.getLeastSignificantBits(); }
    private static UUID uuidFromLong(Long id) { return id == null ? null : UsuarioJpaAdapter.uuidFromLong(id); }
}
