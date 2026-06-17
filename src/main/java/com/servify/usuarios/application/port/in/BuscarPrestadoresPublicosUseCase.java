package com.servify.usuarios.application.port.in;

import com.servify.usuarios.application.dto.PrestadorPublicoResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarPrestadoresPublicosUseCase {

    List<PrestadorPublicoResult> buscarPorNombreUsuario(String nombreUsuario);

    Optional<PrestadorPublicoResult> obtenerPorUsuarioId(UUID usuarioId);
}
