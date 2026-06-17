package com.servify.usuarios.domain.service;

import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;

public class PoliticaPerfilCompleto {

    /**
     * Determina si un perfil cumple con los datos obligatorios definidos por Servify.
     */
    public boolean evaluar(PerfilUsuario perfil) {
        return perfil != null && perfil.estaCompleto();
    }

    public boolean evaluar(Usuario usuario, PerfilUsuario perfil) {
        return usuario != null
                && usuario.getContacto() != null
                && usuario.getContacto().emailValido()
                && evaluar(perfil);
    }
}
