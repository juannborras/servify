package com.servify.administracion.application.port.in;

import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;

import java.util.List;

public interface ListarPublicacionesAdminUseCase {

    List<PublicacionServicioResult> listarPorEstado(EstadoPublicacion estado);
}
