ALTER TABLE public.solicitud_servicio
    ADD COLUMN IF NOT EXISTS tipo_programacion varchar(30) NOT NULL DEFAULT 'inmediata',
    ADD COLUMN IF NOT EXISTS fecha_programada_inicio timestamp without time zone,
    ADD COLUMN IF NOT EXISTS fecha_programada_fin timestamp without time zone;

CREATE TABLE IF NOT EXISTS public.servicio_encuentro (
    id uuid NOT NULL,
    solicitud_id bigint NOT NULL,
    asignacion_servicio_id bigint,
    propuesto_por_id bigint NOT NULL,
    fecha_inicio timestamp without time zone NOT NULL,
    fecha_fin timestamp without time zone NOT NULL,
    estado varchar(30) NOT NULL,
    mensaje varchar(500),
    fecha_resolucion timestamp without time zone,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    updated_at timestamp without time zone NOT NULL DEFAULT now()
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'servicio_encuentro_pkey'
    ) THEN
        ALTER TABLE ONLY public.servicio_encuentro
            ADD CONSTRAINT servicio_encuentro_pkey PRIMARY KEY (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_encuentro_solicitud'
    ) THEN
        ALTER TABLE ONLY public.servicio_encuentro
            ADD CONSTRAINT fk_servicio_encuentro_solicitud
            FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_encuentro_asignacion'
    ) THEN
        ALTER TABLE ONLY public.servicio_encuentro
            ADD CONSTRAINT fk_servicio_encuentro_asignacion
            FOREIGN KEY (asignacion_servicio_id) REFERENCES public.asignacion_servicio(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_encuentro_proponente'
    ) THEN
        ALTER TABLE ONLY public.servicio_encuentro
            ADD CONSTRAINT fk_servicio_encuentro_proponente
            FOREIGN KEY (propuesto_por_id) REFERENCES public.usuario(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_servicio_encuentro_solicitud_fecha
    ON public.servicio_encuentro USING btree (solicitud_id, fecha_inicio);

CREATE INDEX IF NOT EXISTS idx_servicio_encuentro_asignacion
    ON public.servicio_encuentro USING btree (asignacion_servicio_id);

CREATE TABLE IF NOT EXISTS public.servicio_recurrencia (
    id uuid NOT NULL,
    solicitud_id bigint NOT NULL,
    asignacion_servicio_id bigint,
    frecuencia varchar(30) NOT NULL,
    dia_semana varchar(20) NOT NULL,
    hora_desde time without time zone NOT NULL,
    hora_hasta time without time zone NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date,
    estado varchar(30) NOT NULL,
    cancelada_por_id bigint,
    fecha_cancelacion timestamp without time zone,
    motivo_cancelacion varchar(500),
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    updated_at timestamp without time zone NOT NULL DEFAULT now()
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'servicio_recurrencia_pkey'
    ) THEN
        ALTER TABLE ONLY public.servicio_recurrencia
            ADD CONSTRAINT servicio_recurrencia_pkey PRIMARY KEY (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_servicio_recurrencia_solicitud'
    ) THEN
        ALTER TABLE ONLY public.servicio_recurrencia
            ADD CONSTRAINT uq_servicio_recurrencia_solicitud UNIQUE (solicitud_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_recurrencia_solicitud'
    ) THEN
        ALTER TABLE ONLY public.servicio_recurrencia
            ADD CONSTRAINT fk_servicio_recurrencia_solicitud
            FOREIGN KEY (solicitud_id) REFERENCES public.solicitud_servicio(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_recurrencia_asignacion'
    ) THEN
        ALTER TABLE ONLY public.servicio_recurrencia
            ADD CONSTRAINT fk_servicio_recurrencia_asignacion
            FOREIGN KEY (asignacion_servicio_id) REFERENCES public.asignacion_servicio(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_servicio_recurrencia_cancelada_por'
    ) THEN
        ALTER TABLE ONLY public.servicio_recurrencia
            ADD CONSTRAINT fk_servicio_recurrencia_cancelada_por
            FOREIGN KEY (cancelada_por_id) REFERENCES public.usuario(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_servicio_recurrencia_estado
    ON public.servicio_recurrencia USING btree (estado);
