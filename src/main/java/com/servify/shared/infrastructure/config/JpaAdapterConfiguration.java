package com.servify.shared.infrastructure.config;

import com.servify.autenticacion.application.dto.TokenClaims;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.application.dto.TokenResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Configuración de beans que no dependen de persistencia:
 * PasswordHasher y TokenProvider.
 * Los adapters JPA se registran solos via @Component.
 */
@Configuration
public class JpaAdapterConfiguration {

    @Bean
    public PasswordHasherPort passwordHasherPort() {
        return new Sha256PasswordHasher();
    }

    @Bean
    public TokenProviderPort tokenProviderPort() {
        return new SimpleTokenProvider();
    }

    private static class Sha256PasswordHasher implements PasswordHasherPort {
        @Override
        public String hashear(String passwordPlano) {
            return "sha256:" + sha256(passwordPlano == null ? "" : passwordPlano);
        }

        @Override
        public boolean coincide(String passwordPlano, String passwordHash) {
            return Objects.equals(hashear(passwordPlano), passwordHash);
        }
    }

    private static class SimpleTokenProvider implements TokenProviderPort {
        private static final String ACCESS_PREFIX = "access";
        private static final Base64.Encoder B64_ENCODER = Base64.getUrlEncoder().withoutPadding();
        private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();
        private final byte[] signingSecret = cargarSigningSecret();

        @Override
        public TokenResult generarAccessToken(UUID usuarioId, String emailAcceso) {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime expiracion = ahora.plusMinutes(30);
            return new TokenResult(
                    construirAccessToken(usuarioId, emailAcceso, ahora, expiracion),
                    "Bearer", ahora, expiracion);
        }

        @Override
        public TokenResult generarRefreshToken(UUID usuarioId, String emailAcceso) {
            LocalDateTime ahora = LocalDateTime.now();
            return new TokenResult(
                    "refresh-" + usuarioId + "-" + UUID.randomUUID(),
                    "Bearer", ahora, ahora.plusDays(7));
        }

        @Override
        public String obtenerHashToken(String token) {
            return sha256(token == null ? "" : token);
        }

        @Override
        public Optional<TokenClaims> validarAccessToken(String token) {
            if (token == null || token.isBlank()) {
                return Optional.empty();
            }

            String[] parts = token.trim().split("\\.");
            if (parts.length != 3 || !ACCESS_PREFIX.equals(parts[0])) {
                return Optional.empty();
            }

            String payload = parts[1];
            String signature = parts[2];
            String expectedSignature = firmar(ACCESS_PREFIX + "." + payload);
            if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            try {
                String decoded = new String(B64_DECODER.decode(payload), StandardCharsets.UTF_8);
                String[] claims = decoded.split("\\|", -1);
                if (claims.length != 5) {
                    return Optional.empty();
                }

                UUID usuarioId = UUID.fromString(claims[0]);
                String emailAcceso = new String(B64_DECODER.decode(claims[1]), StandardCharsets.UTF_8);
                LocalDateTime fechaEmision = fromEpoch(Long.parseLong(claims[2]));
                LocalDateTime fechaExpiracion = fromEpoch(Long.parseLong(claims[3]));
                if (fechaExpiracion.isBefore(LocalDateTime.now())) {
                    return Optional.empty();
                }

                return Optional.of(new TokenClaims(usuarioId, emailAcceso, fechaEmision, fechaExpiracion));
            } catch (RuntimeException exception) {
                return Optional.empty();
            }
        }

        private String construirAccessToken(UUID usuarioId, String emailAcceso, LocalDateTime fechaEmision, LocalDateTime fechaExpiracion) {
            String email = B64_ENCODER.encodeToString((emailAcceso == null ? "" : emailAcceso).getBytes(StandardCharsets.UTF_8));
            String payload = String.join("|",
                    usuarioId.toString(),
                    email,
                    String.valueOf(toEpoch(fechaEmision)),
                    String.valueOf(toEpoch(fechaExpiracion)),
                    UUID.randomUUID().toString()
            );
            String encodedPayload = B64_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            return ACCESS_PREFIX + "." + encodedPayload + "." + firmar(ACCESS_PREFIX + "." + encodedPayload);
        }

        private String firmar(String value) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(signingSecret, "HmacSHA256"));
                return B64_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception exception) {
                throw new IllegalStateException("No se pudo firmar el token", exception);
            }
        }

        private static byte[] cargarSigningSecret() {
            String configured = System.getenv("SERVIFY_TOKEN_SECRET");
            if (configured != null && !configured.isBlank()) {
                return configured.getBytes(StandardCharsets.UTF_8);
            }

            byte[] generated = new byte[64];
            new SecureRandom().nextBytes(generated);
            return generated;
        }

        private static long toEpoch(LocalDateTime value) {
            return value.toInstant(ZoneOffset.UTC).getEpochSecond();
        }

        private static LocalDateTime fromEpoch(long value) {
            return LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC);
        }
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular SHA-256", e);
        }
    }
}
