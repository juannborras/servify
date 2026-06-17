package com.servify.publicaciones.application.port.in;

import com.servify.publicaciones.application.dto.CategoriaServicioResult;

import java.util.List;

public interface ListarCategoriasUseCase {

    List<CategoriaServicioResult> listarTodas();
}
