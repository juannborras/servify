package com.servify.publicaciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PublicacionZonaCoberturaJpaRepository extends JpaRepository<PublicacionZonaCoberturaJpaEntity, Long> {
    List<PublicacionZonaCoberturaJpaEntity> findByPublicacionId(Long publicacionId);
    void deleteByPublicacionId(Long publicacionId);
}
