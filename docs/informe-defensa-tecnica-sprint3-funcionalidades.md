# Informe para defensa tecnica - Sprint 3 Servify

Fecha: 17 de junio de 2026

## 1. Idea principal para explicar

El Sprint 3 toma el MVP funcional de marketplace y lo acerca a una app usable en contexto real. Ya no se trata solo de registrar usuarios, publicar servicios y hacer matching; se agregaron mecanismos de seguridad, recuperacion de cuenta, notificaciones, reputacion con comentarios, chat, experiencia mobile, PWA y administracion.

La defensa tecnica puede resumirse asi:

- El flujo central del MVP sigue igual: solicitud, matching, respuesta del prestador, confirmacion, finalizacion y calificacion.
- Se agregaron capas de producto necesarias para uso real: recuperacion de contrasena, notificaciones, chat, perfil publico y filtros.
- Se reforzo administracion: moderacion de usuarios/publicaciones y gestion de categorias desde panel admin.
- La base evoluciona con Flyway, evitando parches manuales sueltos.
- Lo nuevo se valido con build frontend y tests backend: 28 tests exitosos.

## 2. Recuperacion de cuenta

Funcionalidad:

- El usuario solicita recuperar contrasena con el email registrado.
- Backend genera un token temporal.
- Se envia un link de recuperacion al email configurado.
- El usuario carga una nueva contrasena.
- La nueva contrasena valida los mismos criterios que en el registro.
- El email pasa a ser obligatorio para considerar el perfil completo.

Endpoints principales:

- `POST /api/v1/auth/password-reset`
- `POST /api/v1/auth/password-reset/confirm`

Validaciones importantes:

- Si el token no existe, vencio o ya fue usado, no permite cambiar la contrasena.
- Si la nueva contrasena no cumple reglas, se rechaza.
- En local puede exponerse token debug solo si el entorno lo permite.
- En produccion debe usarse SMTP real, por ejemplo el mail oficial `servifycommunity@gmail.com` con app password.

Frase para defensa:

> Implementamos recuperacion por email con token temporal y vencimiento. No guardamos la nueva contrasena directamente desde el frontend; el backend valida token y politica de contrasena antes de persistir el cambio.

## 3. Notificaciones internas

Funcionalidad:

- Las notificaciones se persisten en base de datos.
- Se consultan desde la campana o desde configuracion.
- Se pueden marcar como leidas.
- Se pueden eliminar individualmente cuando son viejas.
- Los textos ahora son compactos para no saturar mobile.

Eventos que notifican:

- Moderacion de publicaciones o cuentas.
- Matching de una solicitud con prestador.
- Aceptacion/rechazo de solicitud.
- Contraoferta emitida y resuelta.
- Cancelacion de solicitud.
- Confirmacion de asignacion.
- Confirmacion/finalizacion de servicio.
- Calificacion recibida.
- Nuevo mensaje de chat.

Endpoints principales:

- `GET /api/v1/usuarios/{usuarioId}/notificaciones`
- `PATCH /api/v1/usuarios/{usuarioId}/notificaciones/{notificacionId}/lectura`
- `DELETE /api/v1/usuarios/{usuarioId}/notificaciones/{notificacionId}`

Punto tecnico clave:

- Son notificaciones in-app persistidas, no push nativas.
- Para push real faltaria Web Push o integracion nativa.
- La autorizacion impide que un usuario consulte o elimine notificaciones de otro usuario.

Frase para defensa:

> Priorizamos notificaciones internas persistidas porque garantizan trazabilidad aunque el usuario no este conectado. Push real queda como siguiente evolucion, pero el modelo de eventos ya esta preparado.

## 4. PWA instalable

Funcionalidad:

- La app puede instalarse desde navegador en Android y iOS como web app.
- Se agrego manifest, iconos y service worker basico.
- En mobile queda como icono en pantalla de inicio.

Archivos relevantes:

- `frontend/index.html`
- `frontend/public/manifest.webmanifest`
- `frontend/public/sw.js`
- `frontend/public/icons/*`

Validacion:

- En Android: Chrome muestra opcion "Instalar app" o "Agregar a pantalla principal".
- En iOS: Safari permite "Agregar a pantalla de inicio".

Punto tecnico clave:

- No es una app nativa ni APK/IPA.
- Es una Progressive Web App: usa la misma web desplegada, con metadata e instalacion desde navegador.

## 5. Contraofertas y flujo de solicitud

Funcionalidad:

- El prestador puede contraofertar con precio y comentario.
- El cliente ve la contraoferta.
- Antes de aceptar el precio puede elegir "Seguir buscando".
- Si acepta, el flujo avanza a confirmacion de prestador/asignacion.
- Si sigue buscando, la solicitud vuelve a buscar alternativas.

Endpoints principales:

- `POST /api/v1/distribuciones/{distribucionSolicitudId}/contraofertas`
- `POST /api/v1/contraofertas/{contraofertaId}/resoluciones`
- `POST /api/v1/solicitudes/{solicitudId}/distribuciones/reintentos`

Validaciones:

- La contraoferta queda asociada a la distribucion/solicitud.
- El comentario de contraoferta persiste y llega en notificacion.
- Una solicitud aceptada ya no muestra acciones incompatibles como editar o repetir.

Frase para defensa:

> Separar "seguir buscando" de "cancelar" mejora el estado del dominio: cancelar cierra la solicitud, seguir buscando mantiene la necesidad activa y reintenta matching.

## 6. Chat interno

Funcionalidad:

- El chat se asocia a una solicitud.
- Solo pueden usarlo las partes vinculadas a esa solicitud.
- Se abre en pantalla completa para uso mobile.
- No queda embebido dentro del detalle, para no cargar la pantalla.
- Cada mensaje se persiste.
- El destinatario recibe notificacion in-app.

Endpoints principales:

- `GET /api/v1/solicitudes/{solicitudId}/chat`
- `POST /api/v1/solicitudes/{solicitudId}/chat`

Punto tecnico clave:

- Es chat de tiempo casi real por consulta periodica desde frontend.
- No usa WebSocket todavia.
- Para una version productiva de mayor escala, WebSocket o SSE seria la siguiente mejora.

Frase para defensa:

> La decision fue implementar primero chat persistido y autorizado. Tiempo real estricto se puede optimizar despues con WebSocket, pero la integridad del dato y los permisos ya estan resueltos.

## 7. Calificaciones con comentario y perfil publico

Funcionalidad:

- La calificacion ahora incluye puntaje y comentario.
- El comentario queda asociado a la solicitud/servicio calificado.
- El perfil publico muestra algunas resenas destacadas.
- Hay opcion para ver todas las calificaciones/comentarios.
- Los servicios activos del perfil se ocultan de entrada y se muestran con "Ver servicios activos".

Endpoints principales:

- `POST /api/v1/solicitudes/{solicitudId}/calificaciones`
- `GET /api/v1/solicitudes/{solicitudId}/calificaciones`
- `GET /api/v1/prestadores/{usuarioId}`

Validaciones:

- Solo se puede calificar cuando corresponde por estado del servicio.
- No se puede duplicar una calificacion para el mismo rol.
- El comentario se recupera junto con el puntaje.
- El perfil publico prioriza confianza sin saturar la pantalla.

Frase para defensa:

> La reputacion deja de ser solo numerica. El comentario queda trazado contra un servicio real, por eso es mas confiable que una resena suelta.

## 8. Historial y pantallas menos cargadas

Funcionalidad:

- Historial de solicitudes/servicios con filtros:
  - activas,
  - ultimos 30 dias,
  - ultimos 90 dias,
  - todas.
- Acciones visibles segun estado real.
- En una solicitud aceptada ya no aparecen botones que generarian confusion.
- Cuando el usuario involucrado es el mismo, se muestra "Tu" para mejorar lectura.

Punto tecnico clave:

- No se borra informacion historica.
- Solo se filtra la visualizacion para reducir carga cognitiva en mobile.

## 9. Administracion y categorias

Funcionalidad:

- El admin puede moderar usuarios y publicaciones.
- El admin puede ver detalle de usuario y sus publicaciones.
- El admin puede ver perfil publico del prestador.
- El admin puede gestionar categorias:
  - listar,
  - crear,
  - desactivar,
  - reactivar.

Endpoints principales:

- `GET /api/v1/admin/categorias`
- `POST /api/v1/admin/categorias`
- `PATCH /api/v1/admin/categorias/{categoriaId}/estado`

Seguridad:

- El panel admin exige usuario autenticado, activo y con rol admin.
- El backend valida token y rol antes de ejecutar acciones.
- El secreto `SERVIFY_TOKEN_SECRET` queda configurado por entorno.
- La cuenta oficial recomendada es `servifycommunity@gmail.com`.

Decision sobre multicategoria:

- Se analizo permitir varias categorias por publicacion/solicitud.
- Se dejo pendiente porque requiere migracion de modelo relacional y ajuste del matching.
- Hacerlo apurado podria romper compatibilidad de busqueda y distribucion.

Frase para defensa:

> Para categorias implementamos administracion segura sin tocar base manualmente. Multicategoria queda como evolucion porque cambia cardinalidades y matching; preferimos no degradar estabilidad del MVP.

## 10. Validacion tecnica realizada

Comandos ejecutados:

```text
npm run build
.\mvnw.cmd test
```

Resultado:

- Frontend compila correctamente.
- Backend ejecuta 28 tests sin fallos.
- No quedaron procesos locales levantados.

Escenarios que conviene demostrar manualmente:

1. Recuperar contrasena con un email registrado.
2. Entrar como usuario, revisar notificaciones y borrar una vieja.
3. Crear solicitud, recibir contraoferta y elegir aceptar o seguir buscando.
4. Abrir chat desde una solicitud vinculada.
5. Finalizar servicio y calificar con comentario.
6. Ver el perfil publico y abrir comentarios/servicios activos.
7. Entrar como admin y crear/desactivar/reactivar una categoria.

## 11. Limitaciones honestas para mencionar

- Las notificaciones son internas, no push nativas.
- El chat es persistido y casi real por polling, no WebSocket.
- La PWA no reemplaza una app nativa publicada en stores.
- Multicategoria quedo pendiente por impacto en modelo y matching.
- Reportes/denuncias y auditoria admin son la siguiente prioridad funcional.

## 12. Cierre para defensa oral

El mensaje de cierre puede ser:

> En este sprint fortalecimos Servify como producto: mejoramos seguridad de cuenta, comunicacion entre usuarios, reputacion publica, experiencia mobile y administracion. A nivel tecnico, consolidamos cambios con migraciones Flyway, mantuvimos validaciones backend/frontend y dejamos claro que las mejoras futuras principales son denuncias, auditoria, push real, WebSocket y multicategoria.
