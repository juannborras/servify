package com.servify.usuarios.application.service;

import java.util.Optional;
import java.util.UUID;

import com.servify.shared.domain.exception.ValidationException;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.usuarios.application.dto.ConfiguracionCuentaResult;
import com.servify.usuarios.application.dto.PerfilUsuarioResult;
import com.servify.usuarios.application.dto.UbicacionResult;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.ObtenerConfiguracionCuentaUseCase;
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.Contacto;
import com.servify.usuarios.domain.valueobject.NombreCompleto;

/**
 * Caso de uso de consulta para obtener la configuracion de cuenta.
 */
public class ObtenerConfiguracionCuentaService implements ObtenerConfiguracionCuentaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort;

    public ObtenerConfiguracionCuentaService(UsuarioRepositoryPort usuarioRepositoryPort,
                                             PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.perfilUsuarioRepositoryPort = perfilUsuarioRepositoryPort;
    }

    /**
     * Obtiene la configuracion de cuenta de un usuario.
     *
     * Combina datos de Usuario y PerfilUsuario en un unico resultado para
     * pantallas de configuracion. Si el perfil todavia no existe, el resultado
     * se devuelve con perfil null.
     */
    @Override
    public Optional<ConfiguracionCuentaResult> obtenerPorUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            throw new ValidationException("El usuarioId es obligatorio");
        }

        return usuarioRepositoryPort.buscarPorId(usuarioId)
                .map(usuario -> {
                    UsuarioResult usuarioResult = construirUsuarioResult(usuario);

                    PerfilUsuarioResult perfilResult = perfilUsuarioRepositoryPort.buscarPorUsuarioId(usuarioId)
                            .map(this::construirPerfilResult)
                            .orElse(null);

                    return construirResultado(usuarioResult, perfilResult);
                });
    }

    protected UsuarioResult construirUsuarioResult(Usuario usuario) {
        // Mapea Usuario a UsuarioResult.
        if (usuario == null) {
            throw new ValidationException("El usuario es obligatorio");
        }

        Contacto contacto = usuario.getContacto();

        return new UsuarioResult(
                usuario.getId(),
                contacto != null ? contacto.getEmail() : null,
                usuario.getNombreUsuario(),
                contacto != null ? contacto.getTelefono() : null,
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getEstadoValidacionIdentidad(),
                usuario.getFechaRegistro()
        );
    }

    protected PerfilUsuarioResult construirPerfilResult(PerfilUsuario perfilUsuario) {
        // Mapea PerfilUsuario a PerfilUsuarioResult.
        if (perfilUsuario == null) {
            return null;
        }

        NombreCompleto nombreCompleto = perfilUsuario.getNombreCompleto();

        return PerfilUsuarioResult.builder()
                .id(perfilUsuario.getId())
                .usuarioId(perfilUsuario.getUsuarioId())
                .nombre(nombreCompleto != null ? nombreCompleto.getNombre() : null)
                .apellido(nombreCompleto != null ? nombreCompleto.getApellido() : null)
                .edad(perfilUsuario.getEdad())
                .fotoPerfilUrl(perfilUsuario.getFotoPerfilUrl())
                .ubicacion(construirUbicacionResult(perfilUsuario.getUbicacion()))
                .descripcionPersonal(perfilUsuario.getDescripcionPersonal())
                .perfilCompleto(perfilUsuario.getPerfilCompleto())
                .build();
    }

    protected UbicacionResult construirUbicacionResult(Ubicacion ubicacion) {
        // Mapea Ubicacion del dominio al DTO de salida.
        if (ubicacion == null) {
            return null;
        }

        return new UbicacionResult(
                ubicacion.getPais(),
                ubicacion.getProvincia(),
                ubicacion.getCiudad(),
                ubicacion.getLocalidad(),
                ubicacion.getCalle(),
                ubicacion.getAltura(),
                ubicacion.getReferencia(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud()
        );
    }

    protected ConfiguracionCuentaResult construirResultado(UsuarioResult usuarioResult,
                                                           PerfilUsuarioResult perfilUsuarioResult) {
        // Combina los datos de cuenta y perfil en un unico DTO.
        if (usuarioResult == null) {
            throw new ValidationException("El resultado de usuario es obligatorio");
        }

        return new ConfiguracionCuentaResult(usuarioResult, perfilUsuarioResult);
    }
}
