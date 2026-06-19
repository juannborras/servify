# Limpieza de datos de prueba en Railway

Fecha: 17 de junio de 2026

## Patch agregado

Archivo:

```text
src/main/resources/db/migration/V14__limpieza_datos_prueba_railway.sql
```

Flyway lo ejecuta automaticamente una sola vez cuando el backend se despliega con esa migracion pendiente.

## Que conserva

- `usuario`
- `credencial_acceso`
- `identidad_externa`
- `perfil_usuario`
- `categoria_servicio`
- `publicacion_servicio`
- `disponibilidad_horaria`
- `publicacion_zona_cobertura`
- `configuracion_general`

Esto mantiene las cuentas existentes, datos de perfil y publicaciones cargadas.

## Que borra

- Solicitudes y su flujo completo:
  - `solicitud_servicio`
  - `distribucion_solicitud`
  - `asignacion_servicio`
  - `contraoferta`
  - `confirmacion_finalizacion`
- Calificaciones y comentarios:
  - `calificacion`
- Chat:
  - `chat_mensaje`
- Notificaciones:
  - `notificacion_usuario`
- Datos temporales/sesion:
  - `password_reset_token`
  - `refresh_token`
- Historial administrativo de prueba:
  - `medida_administrativa_usuario`

## Consideraciones

- No modifica estados de usuarios.
- No modifica estados de publicaciones.
- Los usuarios pueden volver a iniciar sesion con sus credenciales.
- Al borrar `refresh_token`, las sesiones activas previas quedan invalidadas.
- Si algun usuario quedo bloqueado/suspendido por una prueba, debe reactivarse desde el panel admin porque esta migracion no toca `usuario.estado`.

## Validacion sugerida en Railway

Despues del deploy, revisar conteos:

```sql
SELECT 'usuario' AS tabla, count(*) FROM public.usuario
UNION ALL SELECT 'perfil_usuario', count(*) FROM public.perfil_usuario
UNION ALL SELECT 'publicacion_servicio', count(*) FROM public.publicacion_servicio
UNION ALL SELECT 'solicitud_servicio', count(*) FROM public.solicitud_servicio
UNION ALL SELECT 'calificacion', count(*) FROM public.calificacion
UNION ALL SELECT 'chat_mensaje', count(*) FROM public.chat_mensaje
UNION ALL SELECT 'notificacion_usuario', count(*) FROM public.notificacion_usuario;
```

Resultado esperado:

- Usuarios, perfiles y publicaciones: conservan registros.
- Solicitudes, calificaciones, chat y notificaciones: quedan en `0`.
