BEGIN;

ALTER TABLE public.usuario
    ADD COLUMN IF NOT EXISTS nombre_usuario character varying(50);

WITH base AS (
    SELECT
        id,
        COALESCE(
            NULLIF(
                regexp_replace(
                    lower(split_part(email, '@', 1)),
                    '[^a-z0-9._-]+',
                    '.',
                    'g'
                ),
                ''
            ),
            'usuario'
        ) AS base_name
    FROM public.usuario
    WHERE nombre_usuario IS NULL OR btrim(nombre_usuario) = ''
),
deduplicados AS (
    SELECT
        id,
        CASE
            WHEN row_number() OVER (PARTITION BY base_name ORDER BY id) = 1
                THEN left(base_name, 30)
            ELSE left(base_name, 24) || '.' || row_number() OVER (PARTITION BY base_name ORDER BY id)
        END AS nombre_usuario_generado
    FROM base
)
UPDATE public.usuario u
SET nombre_usuario = d.nombre_usuario_generado
FROM deduplicados d
WHERE u.id = d.id;

ALTER TABLE public.usuario
    ALTER COLUMN nombre_usuario SET NOT NULL;

ALTER TABLE public.usuario
    DROP CONSTRAINT IF EXISTS uq_usuario_nombre_usuario;

ALTER TABLE public.usuario
    ADD CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario);

CREATE INDEX IF NOT EXISTS idx_usuario_nombre_usuario
    ON public.usuario USING btree (nombre_usuario);

COMMIT;
