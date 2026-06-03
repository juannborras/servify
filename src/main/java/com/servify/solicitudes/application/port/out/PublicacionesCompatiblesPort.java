package com.servify.solicitudes.application.port.out;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface PublicacionesCompatiblesPort {

    Map<UUID, UUID> buscarPublicacionesCompatibles(UUID solicitudId,
                                                   UUID categoriaServicioId,
                                                   String descripcionNecesidad,
                                                   ModalidadServicio modalidadRequerida,
                                                   Ubicacion ubicacionRequerida,
                                                   DisponibilidadHoraria disponibilidadRequerida,
                                                   BigDecimal precioMaximo,
                                                   Integer radioBusquedaKm);
}
