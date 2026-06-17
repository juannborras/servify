package com.servify.usuarios.application.service;

import java.util.Optional;
import java.util.UUID;

import com.servify.shared.domain.exception.NotFoundException;
import com.servify.shared.domain.exception.ValidationException;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.usuarios.application.dto.ActualizarPerfilUsuarioCommand;
import com.servify.usuarios.application.dto.PerfilUsuarioResult;
import com.servify.usuarios.application.dto.UbicacionResult;
import com.servify.usuarios.application.port.in.ActualizarPerfilUsuarioUseCase;
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.service.PoliticaPerfilCompleto;
import com.servify.usuarios.domain.valueobject.NombreCompleto;

/**
 * Caso de uso de aplicacion para actualizar o completar el perfil de un usuario.
 */
public class ActualizarPerfilUsuarioService implements ActualizarPerfilUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort;
    private final PoliticaPerfilCompleto politicaPerfilCompleto;

    public ActualizarPerfilUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort,
                                          PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort,
                                          PoliticaPerfilCompleto politicaPerfilCompleto) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.perfilUsuarioRepositoryPort = perfilUsuarioRepositoryPort;
        this.politicaPerfilCompleto = politicaPerfilCompleto;
    }

    /**
     * Actualiza o completa el perfil de un usuario.
     *
     * Flujo:
     * - valida el command recibido
     * - verifica que el usuario exista
     * - busca el perfil actual o crea uno inicial si todavia no existe
     * - aplica los cambios sobre la entidad de dominio
     * - recalcula el estado de perfil completo usando PoliticaPerfilCompleto
     * - persiste perfil y usuario
     * - devuelve el resultado mapeado a PerfilUsuarioResult
     */
    @Override
    public PerfilUsuarioResult actualizar(ActualizarPerfilUsuarioCommand command) {
        validarCommand(command);

        Usuario usuario = obtenerUsuarioExistente(command.getUsuarioId());

        PerfilUsuario perfilUsuario = obtenerPerfilExistente(command.getUsuarioId())
                .orElseGet(() -> crearPerfilInicial(command));

        aplicarActualizaciones(perfilUsuario, command);

        boolean perfilCompleto = evaluarPerfilCompleto(usuario, perfilUsuario);
        actualizarEstadoPerfilCompleto(perfilUsuario, perfilCompleto);

        PerfilUsuario perfilPersistido = perfilUsuarioRepositoryPort.guardar(perfilUsuario);

        asociarPerfilAUsuarioSiCorresponde(usuario, perfilPersistido);
        usuarioRepositoryPort.guardar(usuario);

        return construirResultado(perfilPersistido);
    }

    protected Usuario obtenerUsuarioExistente(UUID usuarioId) {
        // Recupera el usuario por id y falla si no existe.
        if (usuarioId == null) {
            throw new ValidationException("El usuarioId es obligatorio");
        }

        return usuarioRepositoryPort.buscarPorId(usuarioId)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el id informado"));
    }

    protected Optional<PerfilUsuario> obtenerPerfilExistente(UUID usuarioId) {
        // Busca el perfil asociado al usuario. Si no existe, devuelve Optional.empty().
        if (usuarioId == null) {
            throw new ValidationException("El usuarioId es obligatorio");
        }

        return perfilUsuarioRepositoryPort.buscarPorUsuarioId(usuarioId);
    }

    protected PerfilUsuario crearPerfilInicial(ActualizarPerfilUsuarioCommand command) {
        // Crea un perfil vacio para permitir completado progresivo desde la aplicacion.
        if (command == null || command.getUsuarioId() == null) {
            throw new ValidationException("No se puede crear un perfil inicial sin usuarioId");
        }

        return new PerfilUsuario(
                generarIdPerfil(),
                command.getUsuarioId(),
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    protected void aplicarActualizaciones(PerfilUsuario perfilUsuario,
                                          ActualizarPerfilUsuarioCommand command) {
        // Aplica cambios de forma parcial. Un valor null significa "no modificar".
        // En foto y descripcion, si el campo viene vacio se normaliza a null dentro del dominio.
        if (perfilUsuario == null) {
            throw new ValidationException("El perfil a actualizar es obligatorio");
        }

        if (command == null) {
            throw new ValidationException("El command de actualizacion es obligatorio");
        }

        if (campoPresente(command.getNombre()) || campoPresente(command.getApellido())) {
            String nombre = campoPresente(command.getNombre())
                    ? command.getNombre()
                    : obtenerNombreActual(perfilUsuario);

            String apellido = campoPresente(command.getApellido())
                    ? command.getApellido()
                    : obtenerApellidoActual(perfilUsuario);

            perfilUsuario.actualizarNombre(nombre, apellido);
        }

        if (command.getEdad() != null) {
            perfilUsuario.actualizarEdad(command.getEdad());
        }

        if (campoPresente(command.getFotoPerfilUrl())) {
            perfilUsuario.actualizarFotoPerfil(command.getFotoPerfilUrl());
        }

        if (command.getUbicacion() != null) {
            perfilUsuario.actualizarUbicacion(command.getUbicacion());
        }

        if (campoPresente(command.getDescripcionPersonal())) {
            perfilUsuario.actualizarDescripcionPersonal(command.getDescripcionPersonal());
        }
    }

    protected boolean evaluarPerfilCompleto(Usuario usuario, PerfilUsuario perfilUsuario) {
        // Centraliza la regla de negocio en PoliticaPerfilCompleto.
        if (usuario == null) {
            throw new ValidationException("El usuario es obligatorio");
        }
        if (perfilUsuario == null) {
            throw new ValidationException("El perfil es obligatorio");
        }

        return politicaPerfilCompleto.evaluar(usuario, perfilUsuario);
    }

    protected void actualizarEstadoPerfilCompleto(PerfilUsuario perfilUsuario,
                                                  boolean perfilCompleto) {
        // Refleja en la entidad el resultado calculado por la politica de dominio.
        if (perfilUsuario == null) {
            throw new ValidationException("El perfil es obligatorio");
        }

        perfilUsuario.actualizarEstadoPerfilCompleto(perfilCompleto);
    }

    protected void asociarPerfilAUsuarioSiCorresponde(Usuario usuario,
                                                      PerfilUsuario perfilUsuario) {
        // Vincula el perfil al usuario cuando todavia no estaba asociado
        // o cuando la referencia actual no coincide con el perfil persistido.
        if (usuario == null) {
            throw new ValidationException("El usuario es obligatorio");
        }

        if (perfilUsuario == null) {
            throw new ValidationException("El perfil es obligatorio");
        }

        if (usuario.getPerfil() != perfilUsuario) {
            usuario.asociarPerfil(perfilUsuario);
        }
    }

    protected PerfilUsuarioResult construirResultado(PerfilUsuario perfilUsuario) {
        // Mapea la entidad de dominio al DTO de salida usado por la capa de aplicacion.
        if (perfilUsuario == null) {
            throw new ValidationException("El perfil es obligatorio");
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
        // Mapea la ubicacion del dominio al DTO de salida.
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

    protected NombreCompleto construirNombreCompleto(ActualizarPerfilUsuarioCommand command) {
        // Construye el value object a partir del command cuando se necesita validar ambas partes.
        if (command == null) {
            throw new ValidationException("El command es obligatorio");
        }

        NombreCompleto nombreCompleto = new NombreCompleto(
                normalizarTextoObligatorio(command.getNombre(), "El nombre es obligatorio"),
                normalizarTextoObligatorio(command.getApellido(), "El apellido es obligatorio")
        );

        if (!nombreCompleto.esValido()) {
            throw new ValidationException("El nombre completo no es valido");
        }

        return nombreCompleto;
    }

    protected UUID generarIdPerfil() {
        // Genera un identificador simple basado en UUID aleatorio.
        return UUID.randomUUID();
    }

    private void validarCommand(ActualizarPerfilUsuarioCommand command) {
        if (command == null) {
            throw new ValidationException("El command de actualizacion de perfil es obligatorio");
        }

        if (command.getUsuarioId() == null) {
            throw new ValidationException("El usuarioId es obligatorio");
        }
    }

    private String obtenerNombreActual(PerfilUsuario perfilUsuario) {
        return perfilUsuario.getNombreCompleto() != null
                ? perfilUsuario.getNombreCompleto().getNombre()
                : null;
    }

    private String obtenerApellidoActual(PerfilUsuario perfilUsuario) {
        return perfilUsuario.getNombreCompleto() != null
                ? perfilUsuario.getNombreCompleto().getApellido()
                : null;
    }

    private boolean campoPresente(String valor) {
        return valor != null;
    }

    private String normalizarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException(mensajeError);
        }

        return valor.trim();
    }
}
