package com.servify.solicitudes.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SolicitudServicioJpaRepository extends JpaRepository<SolicitudServicioJpaEntity, Long> {
    List<SolicitudServicioJpaEntity> findBySolicitanteId(Long solicitanteId);
}

interface DistribucionSolicitudJpaRepository extends JpaRepository<DistribucionSolicitudJpaEntity, Long> {
    List<DistribucionSolicitudJpaEntity> findBySolicitudId(Long solicitudId);
    List<DistribucionSolicitudJpaEntity> findByPrestadorId(Long prestadorId);
    List<DistribucionSolicitudJpaEntity> findBySolicitudIdAndEstadoIn(Long solicitudId, List<String> estados);
}

interface AsignacionServicioJpaRepository extends JpaRepository<AsignacionServicioJpaEntity, Long> {
    Optional<AsignacionServicioJpaEntity> findBySolicitudId(Long solicitudId);
    List<AsignacionServicioJpaEntity> findByPrestadorId(Long prestadorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select asignacion from AsignacionServicioJpaEntity asignacion where asignacion.id = :asignacionId")
    Optional<AsignacionServicioJpaEntity> findByIdForUpdate(@Param("asignacionId") Long asignacionId);
}

interface CalificacionJpaRepository extends JpaRepository<CalificacionJpaEntity, Long> {
    List<CalificacionJpaEntity> findByAsignacionId(Long asignacionId);
    Optional<CalificacionJpaEntity> findByAsignacionIdAndRolCalificador(Long asignacionId, String rolCalificador);
}

interface ContraofertaJpaRepository extends JpaRepository<ContraofertaJpaEntity, UUID> {
    List<ContraofertaJpaEntity> findByDistribucionSolicitudId(Long distribucionSolicitudId);
    Optional<ContraofertaJpaEntity> findByDistribucionSolicitudIdAndEstado(Long distribucionSolicitudId, String estado);
    List<ContraofertaJpaEntity> findByPrestadorId(Long prestadorId);
}

interface ConfirmacionFinalizacionJpaRepository extends JpaRepository<ConfirmacionFinalizacionJpaEntity, UUID> {
    List<ConfirmacionFinalizacionJpaEntity> findBySolicitudId(Long solicitudId);
    List<ConfirmacionFinalizacionJpaEntity> findByAsignacionServicioId(Long asignacionServicioId);
    Optional<ConfirmacionFinalizacionJpaEntity> findByAsignacionServicioIdAndRolConfirmanteAndEncuentroServicioIdIsNull(
            Long asignacionServicioId, String rolConfirmante);
    List<ConfirmacionFinalizacionJpaEntity> findByEncuentroServicioId(UUID encuentroServicioId);
    Optional<ConfirmacionFinalizacionJpaEntity> findByEncuentroServicioIdAndRolConfirmante(UUID encuentroServicioId, String rolConfirmante);
    List<ConfirmacionFinalizacionJpaEntity> findByConfirmanteId(Long confirmanteId);
}

interface ServicioEncuentroJpaRepository extends JpaRepository<ServicioEncuentroJpaEntity, UUID> {
    List<ServicioEncuentroJpaEntity> findBySolicitudId(Long solicitudId);
    List<ServicioEncuentroJpaEntity> findByAsignacionServicioId(Long asignacionServicioId);
    List<ServicioEncuentroJpaEntity> findByRecurrenciaServicioId(UUID recurrenciaServicioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select encuentro from ServicioEncuentroJpaEntity encuentro where encuentro.id = :encuentroId")
    Optional<ServicioEncuentroJpaEntity> findByIdForUpdate(@Param("encuentroId") UUID encuentroId);
}

interface ServicioRecurrenciaJpaRepository extends JpaRepository<ServicioRecurrenciaJpaEntity, UUID> {
    Optional<ServicioRecurrenciaJpaEntity> findBySolicitudId(Long solicitudId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select recurrencia from ServicioRecurrenciaJpaEntity recurrencia where recurrencia.id = :recurrenciaId")
    Optional<ServicioRecurrenciaJpaEntity> findByIdForUpdate(@Param("recurrenciaId") UUID recurrenciaId);
}
