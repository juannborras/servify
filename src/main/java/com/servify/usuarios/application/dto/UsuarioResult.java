package com.servify.usuarios.application.dto;

import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.EstadoValidacionIdentidad;
import com.servify.usuarios.domain.enumtype.Rol;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsuarioResult {

    private UUID id;
    private String email;
    private String nombreUsuario;
    private String telefono;
    private Rol rol;
    private EstadoUsuario estado;
    private EstadoValidacionIdentidad estadoValidacionIdentidad;
    private LocalDateTime fechaRegistro;

    public UsuarioResult() {
    }

    public UsuarioResult(UUID id,
                         String email,
                         String nombreUsuario,
                         String telefono,
                         Rol rol,
                         EstadoUsuario estado,
                         EstadoValidacionIdentidad estadoValidacionIdentidad,
                         LocalDateTime fechaRegistro) {
        this.id = id;
        this.email = email;
        this.nombreUsuario = nombreUsuario;
        this.telefono = telefono;
        this.rol = rol;
        this.estado = estado;
        this.estadoValidacionIdentidad = estadoValidacionIdentidad;
        this.fechaRegistro = fechaRegistro;
    }

    public UUID getId() {
        return id;
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

    public EstadoUsuario getEstado() {
        return estado;
    }

    public EstadoValidacionIdentidad getEstadoValidacionIdentidad() {
        return estadoValidacionIdentidad;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
