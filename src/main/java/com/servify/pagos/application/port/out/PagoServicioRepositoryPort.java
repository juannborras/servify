package com.servify.pagos.application.port.out;

import com.servify.pagos.domain.model.PagoServicio;

import java.util.Optional;
import java.util.UUID;

public interface PagoServicioRepositoryPort {
    PagoServicio guardar(PagoServicio pago);
    Optional<PagoServicio> buscarPorId(UUID pagoId);
    Optional<PagoServicio> buscarPorIdParaActualizar(UUID pagoId);
    Optional<PagoServicio> buscarPorObjetivo(UUID asignacionServicioId, UUID encuentroServicioId);
    Optional<PagoServicio> buscarPorObjetivoParaActualizar(UUID asignacionServicioId, UUID encuentroServicioId);
    Optional<PagoServicio> buscarPorExternalReference(String externalReference);
    Optional<PagoServicio> buscarPorExternalReferenceParaActualizar(String externalReference);
    Optional<PagoServicio> buscarPorMercadoPagoPaymentId(String paymentId);
}
