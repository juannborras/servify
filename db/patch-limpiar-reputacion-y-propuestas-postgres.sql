-- Patch manual para Railway/PostgreSQL.
--
-- Limpia calificaciones, promedios derivados e historial operativo de propuestas.
-- El promedio de usuarios no esta persistido: se recalcula desde public.calificacion.

TRUNCATE TABLE
    public.chat_mensaje,
    public.notificacion_usuario,
    public.calificacion,
    public.confirmacion_finalizacion,
    public.contraoferta,
    public.asignacion_servicio,
    public.distribucion_solicitud,
    public.solicitud_servicio
RESTART IDENTITY;
