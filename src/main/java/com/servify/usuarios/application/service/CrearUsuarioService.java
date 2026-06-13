package com.servify.usuarios.application.service;

import com.servify.shared.domain.exception.BusinessRuleException;
import com.servify.shared.domain.exception.ValidationException;
import com.servify.usuarios.application.dto.CrearUsuarioCommand;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.in.CrearUsuarioUseCase;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.EstadoValidacionIdentidad;
import com.servify.usuarios.domain.enumtype.Rol;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.Contacto;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class CrearUsuarioService implements CrearUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public CrearUsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    /**
     * Crea una cuenta nueva.
     *
     * Flujo:
     * - valida que el command exista
     * - construye el value object Contacto
     * - verifica que el email no exista previamente
     * - crea la entidad Usuario con estado inicial valido
     * - persiste el usuario
     * - devuelve el resultado mapeado a UsuarioResult
     */
    @Override
    public UsuarioResult crear(CrearUsuarioCommand command) {
        if (command == null) {
            throw new ValidationException("El command de creacion de usuario es obligatorio");
        }

        Contacto contacto = construirContacto(command);

        if (usuarioRepositoryPort.existePorEmail(contacto.getEmail())) {
            throw new BusinessRuleException("Ese email ya tiene un usuario registrado");
        }

        String nombreUsuario = obtenerNombreUsuario(command, contacto.getEmail());
        if (usuarioRepositoryPort.existePorNombreUsuario(nombreUsuario)) {
            throw new BusinessRuleException("Ese nombre de usuario ya esta en uso");
        }

        Usuario usuario = construirUsuario(command, contacto, nombreUsuario);
        Usuario usuarioPersistido = usuarioRepositoryPort.guardar(usuario);

        return construirResultado(usuarioPersistido);
    }

    protected Usuario construirUsuario(CrearUsuarioCommand command) {
        // Construye la entidad con id nuevo, contacto valido y estado inicial.
        if (command == null) {
            throw new ValidationException("El command de creacion de usuario es obligatorio");
        }

        Contacto contacto = construirContacto(command);
        String nombreUsuario = obtenerNombreUsuario(command, contacto.getEmail());
        return construirUsuario(command, contacto, nombreUsuario);
    }

    protected Usuario construirUsuario(CrearUsuarioCommand command, Contacto contacto, String nombreUsuario) {
        return new Usuario(
                generarIdUsuario(),
                nombreUsuario,
                contacto,
                obtenerRolInicialUsuario(),
                obtenerEstadoInicialUsuario(),
                obtenerEstadoInicialValidacionIdentidad(),
                null,
                obtenerFechaRegistroActual()
        );
    }

    protected Contacto construirContacto(CrearUsuarioCommand command) {
        // Construye el value object Contacto a partir del email y telefono del command.
        if (command == null) {
            throw new ValidationException("El command de creacion de usuario es obligatorio");
        }

        String email = normalizarTextoObligatorio(command.getEmail(), "El email es obligatorio");
        String telefono = normalizarTextoOpcional(command.getTelefono());

        Contacto contacto = new Contacto(email, telefono);

        if (!contacto.emailValido()) {
            throw new ValidationException("El email no tiene un formato valido");
        }

        return contacto;
    }

    protected UsuarioResult construirResultado(Usuario usuario) {
        // Mapea la entidad persistida al DTO de salida de la aplicacion.
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

    protected String obtenerNombreUsuario(CrearUsuarioCommand command, String email) {
        String nombreUsuarioExplicito = normalizarTextoOpcional(command.getNombreUsuario());
        if (nombreUsuarioExplicito != null) {
            return normalizarNombreUsuario(nombreUsuarioExplicito);
        }

        String localPart = email != null && email.contains("@") ? email.substring(0, email.indexOf("@")) : "usuario";
        return generarNombreUsuarioDisponible(localPart);
    }

    protected String normalizarNombreUsuario(String valor) {
        String normalizado = normalizarTextoObligatorio(valor, "El nombre de usuario es obligatorio")
                .trim()
                .toLowerCase(Locale.ROOT);
        if (!normalizado.matches("^[a-z0-9._-]{3,30}$")) {
            throw new ValidationException("El nombre de usuario debe tener 3 a 30 caracteres y solo puede usar letras, numeros, punto, guion o guion bajo");
        }
        return normalizado;
    }

    private String generarNombreUsuarioDisponible(String valorBase) {
        String base = normalizarNombreUsuarioGenerado(valorBase);
        String candidato = recortarNombreUsuario(base, "");
        int intento = 2;

        while (usuarioRepositoryPort.existePorNombreUsuario(candidato)) {
            String sufijo = "." + intento;
            candidato = recortarNombreUsuario(base, sufijo) + sufijo;
            intento++;
        }

        return normalizarNombreUsuario(candidato);
    }

    private String normalizarNombreUsuarioGenerado(String valor) {
        String normalizado = normalizarTextoOpcional(valor);
        if (normalizado == null) {
            normalizado = "usuario";
        }

        normalizado = normalizado
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("^[._-]+|[._-]+$", "");

        if (normalizado.length() < 3) {
            normalizado = "usuario" + normalizado;
        }

        return normalizado;
    }

    private String recortarNombreUsuario(String base, String sufijo) {
        int maximoBase = 30 - sufijo.length();
        if (base.length() <= maximoBase) {
            return base;
        }

        return base.substring(0, maximoBase).replaceAll("[._-]+$", "");
    }

    protected EstadoUsuario obtenerEstadoInicialUsuario() {
        // Define el estado con el que se registra una cuenta nueva.
        return EstadoUsuario.ACTIVO;
    }

    protected Rol obtenerRolInicialUsuario() {
        // El registro publico siempre crea usuarios comunes. Las cuentas ADMIN se promueven por operacion segura de base.
        return Rol.USUARIO;
    }

    protected EstadoValidacionIdentidad obtenerEstadoInicialValidacionIdentidad() {
        // Define el estado inicial de validacion de identidad.
        return EstadoValidacionIdentidad.NO_REQUERIDA;
    }

    protected UUID generarIdUsuario() {
        // Genera un identificador simple basado en UUID aleatorio.
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaRegistroActual() {
        // Centraliza la fecha y hora de registro.
        return LocalDateTime.now();
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new ValidationException(mensajeError);
        }

        return valor.trim();
    }
}
