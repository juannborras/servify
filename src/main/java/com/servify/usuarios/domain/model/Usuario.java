package com.servify.usuarios.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.servify.shared.domain.exception.BusinessRuleException;
import com.servify.shared.domain.exception.ValidationException;
import com.servify.shared.domain.model.BaseEntity;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.EstadoValidacionIdentidad;
import com.servify.usuarios.domain.enumtype.Rol;
import com.servify.usuarios.domain.valueobject.Contacto;

/**
 * Entidad de dominio que representa una cuenta de usuario.
 *
 * Usuario concentra la identidad de acceso, el rol, el estado de la cuenta,
 * el estado de validacion de identidad y la asociacion con PerfilUsuario.
 * Los casos de uso deben pedirle a esta entidad que cambie de estado, en vez de
 * modificar sus atributos directamente.
 */
public class Usuario extends BaseEntity {

    private String nombreUsuario;
    private Contacto contacto;
    private Rol rol;
    private EstadoUsuario estado;
    private EstadoValidacionIdentidad estadoValidacionIdentidad;
    private PerfilUsuario perfil;
    private LocalDateTime fechaRegistro;

    protected Usuario() {
    }

    /**
     * Crea un usuario con contacto, rol, estado, validacion de identidad y perfil asociado.
     */
    public Usuario(UUID id,
                   String nombreUsuario,
                   Contacto contacto,
                   Rol rol,
                   EstadoUsuario estado,
                   EstadoValidacionIdentidad estadoValidacionIdentidad,
                   PerfilUsuario perfil,
                   LocalDateTime fechaRegistro) {
        super(id);
        this.nombreUsuario = validarNombreUsuario(nombreUsuario);
        this.contacto = validarContacto(contacto);
        this.rol = validarRol(rol);
        this.estado = validarEstado(estado);
        this.estadoValidacionIdentidad = validarEstadoValidacionIdentidad(estadoValidacionIdentidad);
        this.perfil = validarPerfilAsociado(perfil);
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
    }

    public Rol getRol() {
        return rol;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void actualizarNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = validarNombreUsuario(nombreUsuario);
    }

    public void setRol(Rol rol) {
        actualizarRol(rol);
    }

    public Contacto getContacto() {
        return contacto;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public EstadoValidacionIdentidad getEstadoValidacionIdentidad() {
        return estadoValidacionIdentidad;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean esAdmin() {
        // Devuelve true cuando el usuario posee rol ADMIN.
        return Rol.ADMIN == rol;
    }

    public boolean esUsuarioComun() {
        // Devuelve true cuando el usuario posee rol USUARIO.
        return Rol.USUARIO == rol;
    }

    public void actualizarRol(Rol rol) {
        // Impide que la entidad quede con un rol nulo o invalido.
        this.rol = validarRol(rol);
    }

    public boolean estaActivo() {
        // Indica si la cuenta esta habilitada para operar normalmente.
        return EstadoUsuario.ACTIVO == estado;
    }

    public boolean estaSuspendido() {
        // Indica si la cuenta tiene una restriccion temporal.
        return EstadoUsuario.SUSPENDIDO == estado;
    }

    public boolean estaBloqueado() {
        // Indica si la cuenta esta inhabilitada por una medida mas severa.
        return EstadoUsuario.BLOQUEADO == estado;
    }

    public boolean tienePerfilCompleto() {
        // Usa la marca persistida, pero tambien recalcula para no bloquear perfiles viejos.
        return contacto != null
                && contacto.emailValido()
                && perfil != null
                && (Boolean.TRUE.equals(perfil.getPerfilCompleto()) || perfil.estaCompleto());
    }

    public boolean puedePublicarServicios() {
        // Regla resumida para publicacion: activo, perfil completo e identidad habilitada.
        return estaActivo()
                && tienePerfilCompleto()
                && (identidadValidada() || EstadoValidacionIdentidad.NO_REQUERIDA == estadoValidacionIdentidad);
    }

    public boolean identidadValidada() {
        // Devuelve true solo cuando la validacion fue aprobada.
        return EstadoValidacionIdentidad.VALIDADA == estadoValidacionIdentidad;
    }

    public void actualizarEmail(String email) {
        // Preserva el telefono actual y reconstruye el value object Contacto.
        Contacto nuevoContacto = new Contacto(
                normalizarTextoObligatorio(email, "El email es obligatorio"),
                contacto != null ? contacto.getTelefono() : null
        );

        this.contacto = validarContacto(nuevoContacto);
    }

    public void actualizarTelefono(String telefono) {
        // Preserva el email actual y actualiza el telefono como dato opcional.
        String emailActual = contacto != null ? contacto.getEmail() : null;
        Contacto nuevoContacto = new Contacto(emailActual, normalizarTextoOpcional(telefono));

        this.contacto = validarContacto(nuevoContacto);
    }

    public void asociarPerfil(PerfilUsuario perfil) {
        // Verifica que el perfil exista y que pertenezca al mismo usuario antes de asociarlo.
        this.perfil = validarPerfilAsociado(perfil);
    }

    public void marcarIdentidadComoValidada() {
        // Cambia el estado de validacion a VALIDADA.
        this.estadoValidacionIdentidad = EstadoValidacionIdentidad.VALIDADA;
    }

    public void marcarIdentidadComoPendiente() {
        // Cambia el estado de validacion a PENDIENTE.
        this.estadoValidacionIdentidad = EstadoValidacionIdentidad.PENDIENTE;
    }

    public void marcarIdentidadComoRechazada() {
        // Cambia el estado de validacion a RECHAZADA.
        this.estadoValidacionIdentidad = EstadoValidacionIdentidad.RECHAZADA;
    }

    public void suspender() {
        // Cambia el estado a SUSPENDIDO respetando las reglas de transicion.
        if (estaBloqueado()) {
            throw new BusinessRuleException("No se puede suspender un usuario bloqueado");
        }

        this.estado = EstadoUsuario.SUSPENDIDO;
    }

    public void bloquear() {
        // Cambia el estado a BLOQUEADO.
        this.estado = EstadoUsuario.BLOQUEADO;
    }

    public void activar() {
        // Cambia el estado a ACTIVO. Esta transicion puede ser usada por administracion para reactivar cuentas.
        this.estado = EstadoUsuario.ACTIVO;
    }

    public void desactivar() {
        // Cambia el estado a INACTIVO cuando la transicion es valida.
        if (estaBloqueado()) {
            throw new BusinessRuleException("No se puede desactivar un usuario bloqueado");
        }

        this.estado = EstadoUsuario.INACTIVO;
    }

    private Contacto validarContacto(Contacto contacto) {
        if (contacto == null) {
            throw new ValidationException("El contacto es obligatorio");
        }

        if (!contacto.emailValido()) {
            throw new ValidationException("El email del contacto no es valido");
        }

        return contacto;
    }

    private String validarNombreUsuario(String nombreUsuario) {
        String normalizado = normalizarTextoObligatorio(nombreUsuario, "El nombre de usuario es obligatorio")
                .toLowerCase();
        if (!normalizado.matches("^[a-z0-9._-]{3,30}$")) {
            throw new ValidationException("El nombre de usuario debe tener 3 a 30 caracteres y solo puede usar letras, numeros, punto, guion o guion bajo");
        }
        return normalizado;
    }

    private Rol validarRol(Rol rol) {
        if (rol == null) {
            throw new ValidationException("El rol es obligatorio");
        }

        return rol;
    }

    private EstadoUsuario validarEstado(EstadoUsuario estado) {
        if (estado == null) {
            throw new ValidationException("El estado del usuario es obligatorio");
        }

        return estado;
    }

    private EstadoValidacionIdentidad validarEstadoValidacionIdentidad(EstadoValidacionIdentidad estadoValidacionIdentidad) {
        if (estadoValidacionIdentidad == null) {
            throw new ValidationException("El estado de validacion de identidad es obligatorio");
        }

        return estadoValidacionIdentidad;
    }

    private PerfilUsuario validarPerfilAsociado(PerfilUsuario perfil) {
        if (perfil == null) {
            return null;
        }

        if (getId() == null || perfil.getUsuarioId() == null || !getId().equals(perfil.getUsuarioId())) {
            throw new BusinessRuleException("El perfil no corresponde al usuario");
        }

        return perfil;
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
