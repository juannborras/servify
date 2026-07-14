package com.servify.solicitudes.application.port.out;

import com.servify.solicitudes.domain.model.ServicioEncuentro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicioEncuentroRepositoryPort {

    ServicioEncuentro guardar(ServicioEncuentro encuentro);

    Optional<ServicioEncuentro> buscarPorId(UUID encuentroId);

    /**
     * Obtiene el encuentro con bloqueo de escritura hasta finalizar la transaccion.
     * Los adaptadores sin soporte transaccional conservan el comportamiento normal.
     */
    default Optional<ServicioEncuentro> buscarPorIdParaActualizar(UUID encuentroId) {
        return buscarPorId(encuentroId);
    }

    List<ServicioEncuentro> buscarPorSolicitudId(UUID solicitudId);

    List<ServicioEncuentro> buscarPorAsignacionServicioId(UUID asignacionServicioId);
}
