-- Checkout Pro: un pago por asignacion unica o por encuentro recurrente.
-- Las credenciales y URLs nunca se persisten: solo referencias operativas.

CREATE TABLE IF NOT EXISTS public.pago_servicio (
    id uuid PRIMARY KEY,
    solicitud_id bigint NOT NULL,
    asignacion_servicio_id bigint NOT NULL,
    encuentro_servicio_id uuid,
    solicitante_id bigint NOT NULL,
    monto numeric(14,2) NOT NULL CHECK (monto > 0),
    moneda varchar(3) NOT NULL DEFAULT 'ARS' CHECK (upper(moneda) = 'ARS'),
    estado varchar(20) NOT NULL DEFAULT 'pendiente'
        CHECK (lower(estado) IN ('pendiente', 'aprobado', 'rechazado', 'cancelado', 'error')),
    external_reference varchar(100) NOT NULL UNIQUE,
    mercadopago_preference_id varchar(150) UNIQUE,
    checkout_url varchar(1000),
    mercadopago_payment_id varchar(100) UNIQUE,
    aprobado_en timestamp,
    error_detalle varchar(500),
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT fk_pago_solicitud FOREIGN KEY (solicitud_id)
        REFERENCES public.solicitud_servicio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pago_asignacion FOREIGN KEY (asignacion_servicio_id)
        REFERENCES public.asignacion_servicio(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pago_encuentro FOREIGN KEY (encuentro_servicio_id)
        REFERENCES public.servicio_encuentro(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pago_solicitante FOREIGN KEY (solicitante_id)
        REFERENCES public.usuario(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_pago_asignacion_unica
    ON public.pago_servicio (asignacion_servicio_id)
    WHERE encuentro_servicio_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_pago_encuentro
    ON public.pago_servicio (encuentro_servicio_id)
    WHERE encuentro_servicio_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_pago_solicitud
    ON public.pago_servicio (solicitud_id);

CREATE INDEX IF NOT EXISTS idx_pago_estado
    ON public.pago_servicio (estado);
