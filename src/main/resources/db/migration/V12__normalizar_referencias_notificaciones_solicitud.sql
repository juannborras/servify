CREATE OR REPLACE FUNCTION pg_temp.servify_uuid_from_bigint(value bigint)
RETURNS uuid AS $$
    SELECT (
        '00000000-0000-0000-' ||
        substring(lpad(to_hex(value), 16, '0') from 1 for 4) ||
        '-' ||
        substring(lpad(to_hex(value), 16, '0') from 5 for 12)
    )::uuid;
$$ LANGUAGE SQL IMMUTABLE STRICT;

UPDATE public.notificacion_usuario n
SET referencia_tipo = 'SOLICITUD',
    referencia_id = pg_temp.servify_uuid_from_bigint(d.solicitud_id),
    updated_at = now()
FROM public.contraoferta c
JOIN public.distribucion_solicitud d ON d.id = c.distribucion_solicitud_id
WHERE n.referencia_tipo = 'CONTRAOFERTA'
  AND n.referencia_id = c.id;

UPDATE public.notificacion_usuario n
SET referencia_tipo = 'SOLICITUD',
    referencia_id = pg_temp.servify_uuid_from_bigint(a.solicitud_id),
    updated_at = now()
FROM public.asignacion_servicio a
WHERE n.referencia_tipo = 'ASIGNACION'
  AND n.referencia_id = pg_temp.servify_uuid_from_bigint(a.id);
