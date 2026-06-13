package com.servify.usuarios.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    Optional<UsuarioJpaEntity> findByEmailIgnoreCase(String email);
    Optional<UsuarioJpaEntity> findByNombreUsuarioIgnoreCase(String nombreUsuario);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
}
