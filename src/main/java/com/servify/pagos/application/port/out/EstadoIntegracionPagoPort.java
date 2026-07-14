package com.servify.pagos.application.port.out;

/** Separa disponibilidad de credenciales y obligatoriedad de la politica de pago. */
public interface EstadoIntegracionPagoPort {
    boolean estaHabilitada();

    /**
     * Politica de negocio independiente de la disponibilidad de credenciales.
     * El default conserva compatibilidad con adaptadores/fakes existentes.
     */
    default boolean esObligatoria() {
        return estaHabilitada();
    }
}
