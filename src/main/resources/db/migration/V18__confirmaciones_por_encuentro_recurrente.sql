-- Una recurrencia es el programa; cada servicio_encuentro es una ocurrencia.
-- Las confirmaciones dejan de cerrar la asignacion completa y se vinculan
-- al encuentro concreto para poder repetirse durante toda la serie.

ALTER TABLE public.servicio_encuentro
    ADD COLUMN IF NOT EXISTS recurrencia_servicio_id uuid;

ALTER TABLE public.confirmacion_finalizacion
    ADD COLUMN IF NOT EXISTS encuentro_servicio_id uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_encuentro_recurrencia'
    ) THEN
        ALTER TABLE public.servicio_encuentro
            ADD CONSTRAINT fk_encuentro_recurrencia
            FOREIGN KEY (recurrencia_servicio_id)
            REFERENCES public.servicio_recurrencia(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_confirmacion_encuentro'
    ) THEN
        ALTER TABLE public.confirmacion_finalizacion
            ADD CONSTRAINT fk_confirmacion_encuentro
            FOREIGN KEY (encuentro_servicio_id)
            REFERENCES public.servicio_encuentro(id) ON DELETE CASCADE;
    END IF;
END $$;

UPDATE public.servicio_encuentro AS encuentro
SET recurrencia_servicio_id = recurrencia.id,
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = encuentro.solicitud_id
  AND encuentro.recurrencia_servicio_id IS NULL;

-- Las confirmaciones legacy de una solicitud recurrente corresponden al
-- primer encuentro confirmado que provocaba el cierre incorrecto del parent.
WITH confirmaciones_a_enlazar AS (
    SELECT confirmacion.id AS confirmacion_id,
           (
               SELECT encuentro.id
               FROM public.servicio_encuentro AS encuentro
               WHERE encuentro.solicitud_id = confirmacion.solicitud_id
                 AND encuentro.asignacion_servicio_id = confirmacion.asignacion_servicio_id
                 AND encuentro.recurrencia_servicio_id = recurrencia.id
                 AND lower(encuentro.estado) IN ('confirmado', 'completado')
               ORDER BY encuentro.fecha_inicio ASC
               LIMIT 1
           ) AS encuentro_id
    FROM public.confirmacion_finalizacion AS confirmacion
    JOIN public.servicio_recurrencia AS recurrencia
      ON recurrencia.solicitud_id = confirmacion.solicitud_id
    WHERE confirmacion.encuentro_servicio_id IS NULL
)
UPDATE public.confirmacion_finalizacion AS confirmacion
SET encuentro_servicio_id = enlace.encuentro_id,
    updated_at = now()
FROM confirmaciones_a_enlazar AS enlace
WHERE enlace.confirmacion_id = confirmacion.id
  AND enlace.encuentro_id IS NOT NULL;

ALTER TABLE public.confirmacion_finalizacion
    DROP CONSTRAINT IF EXISTS uq_confirmacion_asignacion_rol;

CREATE UNIQUE INDEX IF NOT EXISTS uq_confirmacion_encuentro_rol
    ON public.confirmacion_finalizacion (encuentro_servicio_id, rol_confirmante)
    WHERE encuentro_servicio_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_confirmacion_asignacion_rol_legacy
    ON public.confirmacion_finalizacion (asignacion_servicio_id, rol_confirmante)
    WHERE encuentro_servicio_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_confirmacion_encuentro
    ON public.confirmacion_finalizacion (encuentro_servicio_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_encuentro_recurrencia_fecha
    ON public.servicio_encuentro (recurrencia_servicio_id, fecha_inicio)
    WHERE recurrencia_servicio_id IS NOT NULL;

-- Si ambas partes ya habian confirmado, el dato correcto es que termino ese
-- encuentro, no la serie completa.
UPDATE public.servicio_encuentro AS encuentro
SET estado = 'completado',
    fecha_resolucion = COALESCE(encuentro.fecha_resolucion, now()),
    updated_at = now()
WHERE encuentro.recurrencia_servicio_id IS NOT NULL
  AND lower(encuentro.estado) = 'confirmado'
  AND EXISTS (
      SELECT 1
      FROM public.confirmacion_finalizacion AS confirmacion
      WHERE confirmacion.encuentro_servicio_id = encuentro.id
        AND confirmacion.confirmada
        AND lower(confirmacion.rol_confirmante) = 'solicitante'
  )
  AND EXISTS (
      SELECT 1
      FROM public.confirmacion_finalizacion AS confirmacion
      WHERE confirmacion.encuentro_servicio_id = encuentro.id
        AND confirmacion.confirmada
        AND lower(confirmacion.rol_confirmante) = 'prestador'
  );

-- Materializa una sola proxima ocurrencia. El runtime repite esta operacion
-- de forma idempotente al completar o cancelar cada encuentro.
WITH series_sin_proxima AS (
    SELECT recurrencia.id AS recurrencia_id,
           recurrencia.solicitud_id,
           recurrencia.asignacion_servicio_id,
           recurrencia.frecuencia,
           recurrencia.hora_desde,
           recurrencia.hora_hasta,
           recurrencia.fecha_fin,
           max(encuentro.fecha_inicio::date) AS ultima_fecha
    FROM public.servicio_recurrencia AS recurrencia
    JOIN public.servicio_encuentro AS encuentro
      ON encuentro.recurrencia_servicio_id = recurrencia.id
    WHERE lower(recurrencia.estado) = 'activa'
    GROUP BY recurrencia.id, recurrencia.solicitud_id, recurrencia.asignacion_servicio_id,
             recurrencia.frecuencia, recurrencia.hora_desde, recurrencia.hora_hasta,
             recurrencia.fecha_fin
    HAVING count(*) FILTER (WHERE lower(encuentro.estado) IN ('propuesto', 'confirmado')) = 0
), proximas AS (
    SELECT serie.*,
           CASE lower(serie.frecuencia)
               WHEN 'semanal' THEN (serie.ultima_fecha + 7)
               WHEN 'quincenal' THEN (serie.ultima_fecha + 14)
               WHEN 'mensual' THEN (serie.ultima_fecha + INTERVAL '1 month')::date
           END AS proxima_fecha
    FROM series_sin_proxima AS serie
)
INSERT INTO public.servicio_encuentro (
    id, solicitud_id, asignacion_servicio_id, recurrencia_servicio_id,
    propuesto_por_id, fecha_inicio, fecha_fin, estado, mensaje,
    fecha_resolucion, created_at, updated_at
)
SELECT md5(proxima.recurrencia_id::text || ':' || proxima.proxima_fecha::text)::uuid,
       proxima.solicitud_id,
       proxima.asignacion_servicio_id,
       proxima.recurrencia_id,
       solicitud.solicitante_id,
       proxima.proxima_fecha + proxima.hora_desde,
       proxima.proxima_fecha + proxima.hora_hasta,
       'confirmado',
       'Siguiente encuentro del servicio recurrente',
       now(),
       now(),
       now()
FROM proximas AS proxima
JOIN public.solicitud_servicio AS solicitud ON solicitud.id = proxima.solicitud_id
WHERE proxima.proxima_fecha IS NOT NULL
  AND (proxima.fecha_fin IS NULL OR proxima.proxima_fecha <= proxima.fecha_fin)
ON CONFLICT DO NOTHING;

-- Si el rango ya no admite otra ocurrencia, la serie termino naturalmente.
WITH agotadas AS (
    SELECT recurrencia.id,
           recurrencia.fecha_fin,
           CASE lower(recurrencia.frecuencia)
               WHEN 'semanal' THEN (max(encuentro.fecha_inicio::date) + 7)
               WHEN 'quincenal' THEN (max(encuentro.fecha_inicio::date) + 14)
               WHEN 'mensual' THEN (max(encuentro.fecha_inicio::date) + INTERVAL '1 month')::date
           END AS proxima_fecha
    FROM public.servicio_recurrencia AS recurrencia
    JOIN public.servicio_encuentro AS encuentro
      ON encuentro.recurrencia_servicio_id = recurrencia.id
    WHERE lower(recurrencia.estado) = 'activa'
      AND recurrencia.fecha_fin IS NOT NULL
    GROUP BY recurrencia.id, recurrencia.fecha_fin, recurrencia.frecuencia
    HAVING count(*) FILTER (WHERE lower(encuentro.estado) IN ('propuesto', 'confirmado')) = 0
)
UPDATE public.servicio_recurrencia AS recurrencia
SET estado = 'finalizada',
    updated_at = now()
FROM agotadas
WHERE agotadas.id = recurrencia.id
  AND agotadas.proxima_fecha > agotadas.fecha_fin;

UPDATE public.asignacion_servicio AS asignacion
SET estado = 'finalizada',
    fecha_finalizacion = COALESCE(asignacion.fecha_finalizacion, now()),
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.asignacion_servicio_id = asignacion.id
  AND lower(recurrencia.estado) = 'finalizada'
  AND lower(asignacion.estado) = 'activa';

UPDATE public.solicitud_servicio AS solicitud
SET estado = 'finalizada',
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = solicitud.id
  AND lower(recurrencia.estado) = 'finalizada'
  AND lower(solicitud.estado) = 'asignada';

-- Reabre parents cerrados por el bug anterior solo cuando el programa sigue
-- activo y tiene una ocurrencia vigente para continuar.
UPDATE public.asignacion_servicio AS asignacion
SET estado = 'activa',
    fecha_finalizacion = NULL,
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.asignacion_servicio_id = asignacion.id
  AND lower(recurrencia.estado) = 'activa'
  AND lower(asignacion.estado) = 'finalizada'
  AND EXISTS (
      SELECT 1 FROM public.servicio_encuentro AS encuentro
      WHERE encuentro.recurrencia_servicio_id = recurrencia.id
        AND lower(encuentro.estado) IN ('propuesto', 'confirmado')
  );

UPDATE public.solicitud_servicio AS solicitud
SET estado = 'asignada',
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = solicitud.id
  AND lower(recurrencia.estado) = 'activa'
  AND lower(solicitud.estado) = 'finalizada'
  AND EXISTS (
      SELECT 1 FROM public.servicio_encuentro AS encuentro
      WHERE encuentro.recurrencia_servicio_id = recurrencia.id
        AND lower(encuentro.estado) IN ('propuesto', 'confirmado')
  );
