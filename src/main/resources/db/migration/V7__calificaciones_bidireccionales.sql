ALTER TABLE public.calificacion
    ADD COLUMN IF NOT EXISTS calificador_id bigint,
    ADD COLUMN IF NOT EXISTS calificado_id bigint,
    ADD COLUMN IF NOT EXISTS rol_calificador character varying(50);

UPDATE public.calificacion c
SET calificador_id = s.solicitante_id,
    calificado_id = a.prestador_id,
    rol_calificador = 'solicitante'
FROM public.asignacion_servicio a
JOIN public.solicitud_servicio s ON s.id = a.solicitud_id
WHERE c.asignacion_id = a.id
  AND (c.calificador_id IS NULL OR c.calificado_id IS NULL OR c.rol_calificador IS NULL);

ALTER TABLE public.calificacion
    ALTER COLUMN calificador_id SET NOT NULL,
    ALTER COLUMN calificado_id SET NOT NULL,
    ALTER COLUMN rol_calificador SET NOT NULL;

ALTER TABLE public.calificacion
    DROP CONSTRAINT IF EXISTS uq_calificacion_asignacion;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_calificacion_rol'
    ) THEN
        ALTER TABLE ONLY public.calificacion
            ADD CONSTRAINT chk_calificacion_rol
            CHECK (rol_calificador IN ('solicitante', 'prestador'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_calificacion_asignacion_rol'
    ) THEN
        ALTER TABLE ONLY public.calificacion
            ADD CONSTRAINT uq_calificacion_asignacion_rol
            UNIQUE (asignacion_id, rol_calificador);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_calificacion_calificador'
    ) THEN
        ALTER TABLE ONLY public.calificacion
            ADD CONSTRAINT fk_calificacion_calificador
            FOREIGN KEY (calificador_id) REFERENCES public.usuario(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_calificacion_calificado'
    ) THEN
        ALTER TABLE ONLY public.calificacion
            ADD CONSTRAINT fk_calificacion_calificado
            FOREIGN KEY (calificado_id) REFERENCES public.usuario(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_calificacion_calificado
    ON public.calificacion USING btree (calificado_id);

CREATE INDEX IF NOT EXISTS idx_calificacion_calificador
    ON public.calificacion USING btree (calificador_id);
