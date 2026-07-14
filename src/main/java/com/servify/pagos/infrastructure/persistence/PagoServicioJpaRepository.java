package com.servify.pagos.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface PagoServicioJpaRepository extends JpaRepository<PagoServicioJpaEntity, UUID> {
    Optional<PagoServicioJpaEntity> findByAsignacionServicioIdAndEncuentroServicioIdIsNull(Long asignacionId);
    Optional<PagoServicioJpaEntity> findByEncuentroServicioId(UUID encuentroId);
    Optional<PagoServicioJpaEntity> findByExternalReference(String externalReference);
    Optional<PagoServicioJpaEntity> findByMercadoPagoPaymentId(String paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pago from PagoServicioJpaEntity pago where pago.id = :pagoId")
    Optional<PagoServicioJpaEntity> findByIdForUpdate(@Param("pagoId") UUID pagoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pago from PagoServicioJpaEntity pago where pago.asignacionServicioId = :asignacionId and pago.encuentroServicioId is null")
    Optional<PagoServicioJpaEntity> findAsignacionForUpdate(@Param("asignacionId") Long asignacionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pago from PagoServicioJpaEntity pago where pago.encuentroServicioId = :encuentroId")
    Optional<PagoServicioJpaEntity> findEncuentroForUpdate(@Param("encuentroId") UUID encuentroId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pago from PagoServicioJpaEntity pago where pago.externalReference = :externalReference")
    Optional<PagoServicioJpaEntity> findExternalReferenceForUpdate(@Param("externalReference") String externalReference);
}
