-- Repara cancelaciones de recurrencia persistidas por versiones que no propagaban
-- el estado a sus visitas, asignacion y solicitud. Los estados terminales se conservan.

UPDATE public.servicio_encuentro AS encuentro
SET estado = 'cancelado',
    fecha_resolucion = COALESCE(encuentro.fecha_resolucion, recurrencia.fecha_cancelacion, now()),
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = encuentro.solicitud_id
  AND lower(recurrencia.estado) = 'cancelada'
  AND lower(encuentro.estado) IN ('propuesto', 'confirmado');

UPDATE public.asignacion_servicio AS asignacion
SET estado = 'cancelada',
    fecha_finalizacion = COALESCE(asignacion.fecha_finalizacion, recurrencia.fecha_cancelacion, now()),
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = asignacion.solicitud_id
  AND lower(recurrencia.estado) = 'cancelada'
  AND lower(asignacion.estado) IN ('pendiente_confirmacion', 'activa');

UPDATE public.solicitud_servicio AS solicitud
SET estado = 'cancelada',
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = solicitud.id
  AND lower(recurrencia.estado) = 'cancelada'
  AND lower(solicitud.estado) IN ('buscando_prestador', 'asignada');
