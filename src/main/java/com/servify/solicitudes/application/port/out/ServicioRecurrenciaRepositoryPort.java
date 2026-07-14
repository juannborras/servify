package com.servify.solicitudes.application.port.out;

import com.servify.solicitudes.domain.model.ServicioRecurrencia;

import java.util.Optional;
import java.util.UUID;

public interface ServicioRecurrenciaRepositoryPort {

    ServicioRecurrencia guardar(ServicioRecurrencia recurrencia);

    Optional<ServicioRecurrencia> buscarPorId(UUID recurrenciaId);

    default Optional<ServicioRecurrencia> buscarPorIdParaActualizar(UUID recurrenciaId) {
        return buscarPorId(recurrenciaId);
    }

    Optional<ServicioRecurrencia> buscarPorSolicitudId(UUID solicitudId);
}
