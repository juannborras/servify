-- Reabre ocurrencias recurrentes futuras que fueron cerradas antes de su fecha.
-- El pago del solicitante se conserva; se elimina solamente la confirmacion
-- prematura del prestador para que pueda confirmar la realizacion al finalizar.

DELETE FROM public.confirmacion_finalizacion AS confirmacion
USING public.servicio_encuentro AS encuentro
WHERE confirmacion.encuentro_servicio_id = encuentro.id
  AND encuentro.recurrencia_servicio_id IS NOT NULL
  AND lower(encuentro.estado) = 'completado'
  AND encuentro.fecha_fin > CURRENT_TIMESTAMP
  AND lower(confirmacion.rol_confirmante) = 'prestador';

UPDATE public.servicio_encuentro AS encuentro
SET estado = 'confirmado',
    updated_at = now()
WHERE encuentro.recurrencia_servicio_id IS NOT NULL
  AND lower(encuentro.estado) = 'completado'
  AND encuentro.fecha_fin > CURRENT_TIMESTAMP;

UPDATE public.servicio_recurrencia AS recurrencia
SET estado = 'activa',
    updated_at = now()
WHERE lower(recurrencia.estado) = 'finalizada'
  AND EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS encuentro
      WHERE encuentro.recurrencia_servicio_id = recurrencia.id
        AND lower(encuentro.estado) = 'confirmado'
        AND encuentro.fecha_fin > CURRENT_TIMESTAMP
  );

UPDATE public.asignacion_servicio AS asignacion
SET estado = 'activa',
    fecha_finalizacion = NULL,
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.asignacion_servicio_id = asignacion.id
  AND lower(recurrencia.estado) = 'activa'
  AND lower(asignacion.estado) = 'finalizada'
  AND EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS encuentro
      WHERE encuentro.recurrencia_servicio_id = recurrencia.id
        AND lower(encuentro.estado) = 'confirmado'
        AND encuentro.fecha_fin > CURRENT_TIMESTAMP
  );

UPDATE public.solicitud_servicio AS solicitud
SET estado = 'asignada',
    updated_at = now()
FROM public.servicio_recurrencia AS recurrencia
WHERE recurrencia.solicitud_id = solicitud.id
  AND lower(recurrencia.estado) = 'activa'
  AND lower(solicitud.estado) = 'finalizada'
  AND EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS encuentro
      WHERE encuentro.recurrencia_servicio_id = recurrencia.id
        AND lower(encuentro.estado) = 'confirmado'
        AND encuentro.fecha_fin > CURRENT_TIMESTAMP
  );

-- Una cita inicial PROGRAMADA representa la solicitud completa. Se reconcilian
-- solo parents aun activos cuya cita inicial esta cancelada y que no poseen
-- ninguna visita abierta o completada de reemplazo.

UPDATE public.asignacion_servicio AS asignacion
SET estado = 'cancelada',
    fecha_finalizacion = COALESCE(asignacion.fecha_finalizacion, now()),
    updated_at = now()
FROM public.solicitud_servicio AS solicitud
WHERE asignacion.solicitud_id = solicitud.id
  AND lower(solicitud.tipo_programacion) = 'programada'
  AND lower(solicitud.estado) = 'asignada'
  AND lower(asignacion.estado) IN ('activa', 'pendiente_confirmacion')
  AND EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS inicial
      WHERE inicial.solicitud_id = solicitud.id
        AND inicial.recurrencia_servicio_id IS NULL
        AND inicial.fecha_inicio = solicitud.fecha_programada_inicio
        AND inicial.fecha_fin = solicitud.fecha_programada_fin
        AND lower(inicial.estado) = 'cancelado'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS vigente
      WHERE vigente.solicitud_id = solicitud.id
        AND lower(vigente.estado) IN ('propuesto', 'confirmado', 'completado')
  );

UPDATE public.solicitud_servicio AS solicitud
SET estado = 'cancelada',
    updated_at = now()
WHERE lower(solicitud.tipo_programacion) = 'programada'
  AND lower(solicitud.estado) = 'asignada'
  AND EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS inicial
      WHERE inicial.solicitud_id = solicitud.id
        AND inicial.recurrencia_servicio_id IS NULL
        AND inicial.fecha_inicio = solicitud.fecha_programada_inicio
        AND inicial.fecha_fin = solicitud.fecha_programada_fin
        AND lower(inicial.estado) = 'cancelado'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM public.servicio_encuentro AS vigente
      WHERE vigente.solicitud_id = solicitud.id
        AND lower(vigente.estado) IN ('propuesto', 'confirmado', 'completado')
  );
