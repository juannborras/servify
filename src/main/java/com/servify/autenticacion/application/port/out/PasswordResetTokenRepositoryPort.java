package com.servify.autenticacion.application.port.out;

import com.servify.autenticacion.domain.model.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken guardar(PasswordResetToken token);

    Optional<PasswordResetToken> buscarPorTokenHash(String tokenHash);

    List<PasswordResetToken> buscarVigentesPorUsuarioId(UUID usuarioId, LocalDateTime ahora);
}
