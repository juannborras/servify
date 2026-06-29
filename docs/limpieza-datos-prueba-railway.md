# Limpieza de datos de prueba en Railway

Fecha: 17 de junio de 2026

## Estado actual

Las limpiezas de datos de prueba quedan retiradas de las migraciones automaticas
de Flyway.

Motivo: `V14` y `V15` fueron parches one-off para limpiar datos operativos
de prueba en Railway. Si quedan bajo `src/main/resources/db/migration`,
Flyway puede ejecutarlos automaticamente en cualquier base donde figuren como
pendientes. Eso no es deseable para una app con persistencia real.

```text
src/main/resources/db/migration/V14__limpieza_datos_prueba_railway.sql
src/main/resources/db/migration/V15__limpiar_reputacion_y_propuestas_railway.sql
```

Es normal que esas versiones sigan apareciendo como aplicadas en
`flyway_schema_history` de bases donde ya corrieron. La configuracion:

```properties
spring.flyway.ignore-migration-patterns=*:future,*:missing
```

permite que Flyway no falle cuando encuentre esas migraciones historicas ya
aplicadas pero retiradas del codigo.

## Patches manuales disponibles

Los SQL quedan disponibles solo para ejecucion manual y consciente:

```text
db/patch-limpieza-datos-prueba-railway-postgres.sql
db/patch-limpiar-reputacion-y-propuestas-postgres.sql
```

No deben moverse nuevamente a `src/main/resources/db/migration`.

El promedio y la cantidad de valoraciones no estan guardados como columnas en `usuario` ni `perfil_usuario`; se calculan desde `calificacion`. Por eso, al limpiar `calificacion`, la reputacion vuelve a cero automaticamente.

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

## Validacion sugerida si se ejecuta manualmente

Despues de ejecutar un patch manual, revisar conteos:

```sql
SELECT 'usuario' AS tabla, count(*) FROM public.usuario
UNION ALL SELECT 'perfil_usuario', count(*) FROM public.perfil_usuario
UNION ALL SELECT 'publicacion_servicio', count(*) FROM public.publicacion_servicio
UNION ALL SELECT 'solicitud_servicio', count(*) FROM public.solicitud_servicio
UNION ALL SELECT 'distribucion_solicitud', count(*) FROM public.distribucion_solicitud
UNION ALL SELECT 'asignacion_servicio', count(*) FROM public.asignacion_servicio
UNION ALL SELECT 'contraoferta', count(*) FROM public.contraoferta
UNION ALL SELECT 'calificacion', count(*) FROM public.calificacion
UNION ALL SELECT 'chat_mensaje', count(*) FROM public.chat_mensaje
UNION ALL SELECT 'notificacion_usuario', count(*) FROM public.notificacion_usuario;
```

Resultado esperado:

- Usuarios, perfiles y publicaciones: conservan registros.
- Solicitudes, distribuciones, asignaciones, contraofertas, calificaciones, chat y notificaciones: quedan en `0`.
