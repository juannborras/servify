package com.servify.usuarios.application.dto;

import com.servify.usuarios.domain.enumtype.Rol;


//Es un DTO de aplicación que representa la entrada del caso de uso “crear usuario”.
public class CrearUsuarioCommand {

    private String email;
    private String nombreUsuario;
    private String telefono;
    private Rol rol;

    public CrearUsuarioCommand() {
    }

    public CrearUsuarioCommand(String email, String telefono, Rol rol) {
        this(email, null, telefono, rol);
    }

    public CrearUsuarioCommand(String email, String nombreUsuario, String telefono, Rol rol) {
        this.email = email;
        this.nombreUsuario = nombreUsuario;
        this.telefono = telefono;
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public Rol getRol() {
        return rol;
    }
}
