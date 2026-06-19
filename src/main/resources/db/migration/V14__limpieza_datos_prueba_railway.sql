-- Limpieza de datos operativos de pruebas en Railway.
--
-- Conserva:
-- - usuarios, credenciales e identidades externas;
-- - perfiles de usuario;
-- - categorias;
-- - publicaciones, disponibilidad y zonas de cobertura;
-- - configuracion general.
--
-- Borra:
-- - solicitudes y todo su flujo operativo;
-- - asignaciones, distribuciones, contraofertas y confirmaciones;
-- - calificaciones y comentarios asociados;
-- - mensajes de chat;
-- - notificaciones;
-- - tokens temporales/sesiones;
-- - medidas administrativas historicas de prueba.

TRUNCATE TABLE
    public.chat_mensaje,
    public.notificacion_usuario,
    public.password_reset_token,
    public.refresh_token,
    public.medida_administrativa_usuario,
    public.calificacion,
    public.confirmacion_finalizacion,
    public.contraoferta,
    public.asignacion_servicio,
    public.distribucion_solicitud,
    public.solicitud_servicio
RESTART IDENTITY;
