package com.servify.solicitudes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
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
    Optional<ConfirmacionFinalizacionJpaEntity> findByAsignacionServicioIdAndRolConfirmante(Long asignacionServicioId, String rolConfirmante);
    List<ConfirmacionFinalizacionJpaEntity> findByConfirmanteId(Long confirmanteId);
}
