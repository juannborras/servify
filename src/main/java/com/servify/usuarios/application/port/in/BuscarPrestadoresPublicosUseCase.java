package com.servify.usuarios.application.port.in;

import com.servify.usuarios.application.dto.PrestadorPublicoResult;

import java.util.List;

public interface BuscarPrestadoresPublicosUseCase {

    List<PrestadorPublicoResult> buscarPorNombreUsuario(String nombreUsuario);
}
