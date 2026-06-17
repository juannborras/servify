# Informe de avances Sprint 3 - Servify

Fecha inicial: 14 de junio de 2026  
Actualizacion: 17 de junio de 2026  
Proyecto: Servify  
Alcance: funcionalidades nuevas agregadas despues de los informes de Sprint 1 y Sprint 2

## 1. Resumen ejecutivo

Durante este tercer sprint se avanzo sobre capas que el MVP necesitaba para sentirse mas cercano a una app real: seguridad de cuenta, recuperacion de contrasena, administracion, moderacion, notificaciones, busqueda de prestadores, perfil publico, configuracion de usuario y pulido visual.

El Sprint 2 ya habia dejado conectado el flujo principal de marketplace: registro, publicaciones, solicitudes, matching, asignacion, finalizacion y calificaciones. Este Sprint 3 no repite ese alcance; se concentra en funcionalidades nuevas que completan la experiencia y preparan releases futuros.

Los avances mas importantes fueron:

- Recuperacion de cuenta por email registrado.
- Nombre de usuario unico, editable y usable para iniciar sesion.
- Busqueda de prestadores por nombre de usuario.
- Perfil publico de prestador mas claro y navegable.
- Boton para repetir solicitudes.
- Barra inferior persistente y boton central para nueva solicitud/publicacion.
- Barra lateral de configuracion funcional.
- Modo oscuro usable en pantallas principales.
- Categorias populares con acceso a todas las categorias.
- Acciones desde publicaciones por categoria: solicitar servicio y ver perfil.
- Administracion segura para moderar usuarios y publicaciones.
- Notificaciones internas persistidas para moderacion y flujo de solicitudes/servicios.
- Migraciones con Flyway para ordenar cambios de base de datos.
- Aplicacion instalable como PWA en Android/iOS desde el navegador.
- Chat interno asociado a solicitudes, con vista full screen para mobile.
- Comentarios de calificacion persistidos y visibles de forma resumida en perfiles publicos.
- Historial de solicitudes/servicios con filtros temporales.
- Administracion de categorias desde el panel Servify.

## 2. Autenticacion, registro y recuperacion de cuenta

Se amplio el flujo de inicio de sesion y registro para cubrir casos reales que no estaban documentados en los sprints anteriores.

Funcionalidades agregadas:

- Recuperacion de contrasena mediante email registrado.
- Pantalla de "Olvide mi contrasena" en el frontend.
- Generacion de token temporal de recuperacion en backend.
- Envio de link de recuperacion por email usando configuracion SMTP.
- Validacion de nueva contrasena con los mismos criterios que el registro.
- Vencimiento configurable del token de recuperacion.
- Modo debug local para exponer token solo en entornos de prueba.
- Email obligatorio para considerar completo el perfil.
- Recalculo de perfiles completos luego de exigir email valido.

Mejoras en registro/login:

- Campo de apellido ubicado debajo del nombre para mejorar el layout mobile.
- Fecha de nacimiento con indicacion mas clara para el usuario.
- Foto/logo de perfil durante registro con opcion de elegir imagen o sacar foto.
- Mensajes diferenciados en login:
  - Si email o nombre de usuario no existe, se informa que no hay cuenta registrada.
  - Si la cuenta existe pero la contrasena es incorrecta, se informa contrasena incorrecta.
- Mensaje especifico cuando un email ya esta registrado.
- Alta de nombre de usuario en registro.
- Validacion de nombre de usuario unico.
- Inicio de sesion con email o nombre de usuario.
- Edicion de nombre de usuario desde configuracion/perfil.
- Registro social con Google generando un nombre de usuario unico a partir del nombre de Google, con variacion automatica si ya existe.

## 3. Busqueda de prestadores y perfiles publicos

Se reemplazo el enfoque inicial de busqueda por categoria en el inicio por una busqueda mas orientada a usuarios prestadores.

Funcionalidades nuevas:

- Busqueda de prestadores por nombre de usuario desde la pantalla de inicio.
- La app no muestra prestadores si el usuario todavia no escribio nada.
- Los resultados aparecen progresivamente al escribir letras o caracteres.
- Los resultados muestran principalmente:
  - nombre visible del prestador,
  - nombre de usuario,
  - cantidad de publicaciones activas,
  - publicaciones activas principales.
- Cada resultado permite abrir el perfil publico del prestador.
- El perfil publico consolida informacion visible del prestador.
- Las publicaciones activas ya no se muestran de entrada para no cargar la pantalla.
- Se agrego la opcion "Ver servicios activos" para desplegarlas solo cuando el usuario lo necesita.
- Las calificaciones con comentario se muestran como destacados breves sobre la reputacion.
- Se agrego la opcion de ver todas las calificaciones/comentarios en una vista emergente.
- Desde categorias y busqueda se puede navegar al perfil publico sin pasar por pantallas intermedias.

Esto adelanta parte del valor de releases futuros: discovery de prestadores, confianza y navegacion publica de perfiles.

## 4. Solicitudes y reutilizacion de datos

Se agrego una mejora directa de productividad para el usuario solicitante:

- Boton "Repetir" en solicitudes existentes.
- Al repetir, se abre la creacion de solicitud precargada con datos de la solicitud anterior.
- Se reutilizan titulo, descripcion, categoria, modalidad, ubicacion, precio y disponibilidad cuando existen.
- La pantalla distingue "Nueva solicitud" de "Repetir solicitud".
- En solicitudes aceptadas, en curso o finalizadas se ocultan acciones que ya no corresponden, como repetir o editar.
- El historial permite filtrar por solicitudes activas, ultimos 30 dias, ultimos 90 dias o todas.
- En contraofertas, "Seguir buscando" aparece antes de aceptar el precio para mantener abierta la busqueda si el precio no sirve.
- Los comentarios de contraoferta persisten y se incluyen en la notificacion al solicitante.

Esto reduce friccion para casos frecuentes como pedir el mismo servicio nuevamente o cargar una solicitud similar.

## 5. Categorias, publicaciones y acciones desde categoria

El inicio y el flujo por categorias se reorganizaron para que la experiencia sea mas limpia en mobile.

Funcionalidades agregadas:

- Se muestran solo categorias populares al inicio.
- Se agrego boton "Ver todas las categorias".
- Al expandir, se muestran todas las categorias disponibles.
- Al entrar a una categoria se listan publicaciones disponibles.
- Cada publicacion de categoria permite:
  - solicitar ese servicio,
  - ver el perfil publico del prestador.
- Si la publicacion pertenece al usuario actual, se indica que es propia.
- La app bloquea solicitar una publicacion propia.
- El usuario si puede solicitar el mismo tipo de servicio a otro prestador.
- Se corrigieron botones visualmente inconsistentes en categorias y publicaciones.

Este cambio hace que la categoria funcione como una pantalla de discovery real y no solo como atajo a crear publicaciones.

## 6. Navegacion mobile, barra inferior y barra lateral

Se mejoro la estructura de navegacion para que la app sea mas coherente en celular.

Funcionalidades agregadas:

- Barra de pestanas inferior visible en todo momento desde mobile, salvo pantallas de splash/login.
- Reordenamiento del boton de accion principal al centro de la barra inferior.
- Intercambio de posicion con "Mis servicios" para que el signo `+` quede mas natural.
- Animaciones fluidas en nuevas opciones de la barra lateral.
- Barra lateral de configuracion accesible desde perfil.
- Opciones funcionales en configuracion:
  - Cuenta,
  - Apariencia,
  - Bandeja de notificaciones,
  - Preferencias de notificaciones,
  - Privacidad,
  - Ayuda,
  - Administracion cuando el usuario es admin,
  - Cerrar sesion.
- Persistencia local de preferencias de apariencia, notificaciones y privacidad.

La pestana de perfil queda enfocada como vista de perfil publico/propio, mientras que configuracion se separa en la barra lateral.

## 7. Modo oscuro y pulido visual

Se trabajo el modo oscuro para que sea una alternativa viable y no solo un cambio estetico parcial.

Mejoras agregadas:

- Selector de apariencia: claro, oscuro o sistema.
- Aplicacion del tema sobre `documentElement` y `color-scheme`.
- Persistencia local de la preferencia.
- Contraste reforzado en:
  - solicitudes,
  - servicios/publicaciones,
  - categorias,
  - panel administrativo,
  - notificaciones,
  - cards,
  - botones,
  - estados vacios,
  - formularios.
- Se reemplazaron botones blancos fuera de tema por variantes con color y contraste.
- Se agregaron clases reutilizables para texto en modo oscuro.
- Se mejoro la legibilidad de la bandeja de notificaciones en dark mode.

Este trabajo deja el modo oscuro apto para demo y uso real basico.

## 8. Administracion, seguridad y moderacion

Se avanzo con la parte administrativa como funcionalidad nueva de release futuro adelantada al MVP.

Funcionalidades agregadas:

- Cuenta administradora separada del uso comun de la app.
- Promocion de la cuenta oficial `servifycommunity@gmail.com` a rol admin mediante migracion.
- Acceso al panel admin solo para usuarios autenticados y activos con rol administrador.
- Validacion de access token antes de permitir acciones admin.
- Uso de secreto de token configurable por entorno (`SERVIFY_TOKEN_SECRET`).
- Proteccion para que el admin no pueda suspender o bloquear su propia cuenta desde el panel.
- Panel administrativo accesible desde configuracion solo cuando la sesion corresponde a admin.
- Vista de usuarios ordenados de peor calificacion a mejor.
- Busqueda de usuarios desde administracion.
- Entrada a detalle de usuario desde el panel.
- En detalle de usuario se pueden ver publicaciones asociadas.
- Moderacion de publicaciones desde el usuario afectado.
- Baja, bloqueo o reactivacion de publicaciones consideradas inapropiadas o inseguras.
- Cambio de estado de usuarios cuando corresponde.
- Acceso desde admin al perfil publico del prestador.
- Visualizacion de informacion completa de publicacion al revisar/moderar.
- Correccion del nombre visible del dueno de publicacion.
- Gestion administrativa de categorias:
  - listado completo de categorias activas e inactivas,
  - alta de nuevas categorias,
  - baja logica/desactivacion de categorias existentes,
  - reactivacion de categorias.
- Se mantuvo una sola categoria por publicacion/solicitud en este sprint para no romper matching ni modelo relacional sin una migracion mayor.

Tambien se agrego el principio de notificar al usuario afectado por cambios administrativos.

## 9. Notificaciones internas persistidas

Sprint 2 mencionaba una campana basica de actividad. En Sprint 3 se avanzo a una bandeja persistida y conectada al backend. Primero se cubrio moderacion y luego se extendio a eventos relevantes del flujo de solicitudes y servicios.

Funcionalidades agregadas:

- Tabla `notificacion_usuario` con usuario, tipo, titulo, mensaje, referencia, estado de lectura y fechas.
- Endpoint para listar notificaciones del usuario autenticado.
- Endpoint para marcar notificaciones como leidas.
- Autorizacion para impedir consultar notificaciones de otro usuario.
- Creacion de notificaciones cuando administracion modera publicaciones.
- Creacion de notificaciones cuando administracion cambia el estado de una cuenta.
- Creacion de notificaciones cuando una solicitud hace matching con un prestador.
- Creacion de notificaciones cuando un prestador acepta o rechaza una solicitud.
- Creacion de notificaciones cuando un prestador emite una contraoferta.
- Creacion de notificaciones cuando el solicitante acepta o rechaza una contraoferta.
- Creacion de notificaciones cuando el solicitante confirma la asignacion del servicio.
- Creacion de notificaciones cuando una solicitud asociada se cancela.
- Creacion de notificaciones cuando una parte confirma finalizacion y cuando el servicio queda finalizado por ambas partes.
- Creacion de notificaciones cuando un usuario recibe una calificacion.
- Bandeja de notificaciones en frontend.
- Contador de notificaciones no leidas.
- Acceso desde la campana del inicio.
- Acceso desde la barra lateral.
- Marcar una notificacion como leida.
- Marcar todas como leidas.
- Eliminar notificaciones viejas desde la bandeja con una accion individual.
- Mensajes mas compactos para no sobrecargar la pantalla: servicio, fecha y parte involucrada cuando aplica.
- Navegacion al contexto relacionado:
  - publicacion -> Mis servicios,
  - cuenta/usuario -> Perfil,
  - solicitud/contraoferta/asignacion -> Solicitudes.
- Notificacion por nuevos mensajes de chat asociados a una solicitud.
- Visualizacion de titulo, mensaje, fecha y estado leido/no leido.

Esto cierra el circuito de comunicacion interna: las acciones importantes quedan registradas como avisos dentro de Servify sin incorporar todavia push notifications.

## 10. Migraciones, Flyway y base de datos

Se ordeno la evolucion de schema y parches SQL mediante Flyway.

Cambios relevantes:

- Se agrego `spring-boot-starter-flyway`.
- Se agrego soporte PostgreSQL de Flyway.
- Flyway corre antes de Hibernate validate.
- Se habilito `baseline-on-migrate` para adoptar bases existentes.
- Las migraciones quedan versionadas en `src/main/resources/db/migration`.

Migraciones destacadas relacionadas con este sprint:

- `V4__username_usuarios.sql`: soporte de nombre de usuario unico.
- `V8__recalcular_perfiles_completos.sql`: recalculo de perfil completo.
- `V9__notificaciones_usuario.sql`: tabla e indices de notificaciones.
- `V10__promote_servify_admin.sql`: promocion de la cuenta oficial a admin usando placeholder `SERVIFY_ADMIN_EMAIL`.
- `V11__recuperacion_password_email.sql`: tokens de recuperacion de contrasena y recalculo de perfil completo con email requerido.
- `V12__normalizar_referencias_notificaciones_solicitud.sql`: referencias de notificaciones preparadas para navegar a solicitudes relacionadas.
- `V13__chat_y_comentarios_calificaciones.sql`: persistencia de mensajes de chat y comentarios de calificaciones.

Variables de entorno nuevas o reforzadas:

- `SERVIFY_TOKEN_SECRET`
- `SERVIFY_ADMIN_EMAIL`
- `SERVIFY_FRONTEND_URL`
- `SERVIFY_PASSWORD_RESET_TTL_MINUTES`
- `SERVIFY_PASSWORD_RESET_EXPOSE_DEBUG_TOKEN`
- `SERVIFY_MAIL_HOST`
- `SERVIFY_MAIL_PORT`
- `SERVIFY_MAIL_USERNAME`
- `SERVIFY_MAIL_PASSWORD`
- `SERVIFY_MAIL_FROM`
- `SERVIFY_MAIL_SMTP_AUTH`
- `SERVIFY_MAIL_SMTP_STARTTLS`

Esto aplica tanto a entorno local con Docker Compose como a deploy en Railway, siempre que las variables esten configuradas en el servicio backend.

## 11. Frontend y pantallas nuevas/modificadas

Pantallas y componentes trabajados:

- `AuthScreen`: registro/login, nombre de usuario, foto, recuperacion de contrasena.
- `ExploreScreen`: busqueda por prestador, categorias populares/todas, campana.
- `RequestsScreen`: repetir solicitud, filtros temporales y acciones segun estado.
- `RequestDetail`: detalle de solicitud, contraofertas, finalizacion, calificacion y apertura de chat.
- `ServiceChat`: chat full screen asociado a una solicitud.
- `NewRequestModal`: precarga para repetir solicitud.
- `CategoryPublicationsScreen`: solicitar servicio, ver perfil, bloquear solicitud propia.
- `PublicProfileScreen`: perfil publico/propio, servicios activos bajo demanda y resenas destacadas.
- `RatingModal`: carga de puntaje y comentario escrito.
- `SettingsDrawer`: barra lateral funcional.
- `AdminPanelScreen`: administracion de usuarios, publicaciones y categorias.
- `NotificationsScreen`: bandeja de notificaciones.
- `BottomNav`: accion central y barra persistente.
- `theme.css`: dark mode y contraste.

## 12. Backend y APIs nuevas/reforzadas

Areas de backend ampliadas:

- Autenticacion:
  - recuperacion de contrasena,
  - validacion de token temporal,
  - envio de email,
  - login por email o nombre de usuario.
- Usuarios:
  - nombre de usuario unico,
  - edicion de cuenta,
  - busqueda publica de prestadores,
  - perfil completo condicionado a email valido.
- Administracion:
  - autorizacion fuerte por token y rol admin,
  - listado/busqueda de usuarios,
  - moderacion de publicaciones,
  - cambio de estado de usuarios,
  - alta, baja logica y reactivacion de categorias,
  - notificacion a afectados.
- Notificaciones:
  - creacion,
  - listado,
  - marcado como leida,
  - eliminacion individual.
- Chat:
  - listado de mensajes por solicitud,
  - envio de mensajes entre solicitante y prestador,
  - validacion de participante autorizado,
  - notificacion al destinatario.
- Calificaciones:
  - comentario escrito persistido,
  - consulta de comentario junto al puntaje,
  - exposicion resumida en perfil publico.

## 13. Pruebas y validaciones realizadas

Validaciones recientes ejecutadas y vigentes al 17 de junio de 2026:

```text
npm run build
.\mvnw.cmd test
```

Resultado:

- Build frontend exitoso.
- Tests backend exitosos: 28 tests, 0 fallos, 0 errores.
- Se verifico que no quedaran procesos locales de prueba corriendo, respetando el uso por Docker.

Cobertura agregada o reforzada:

- Recuperacion de contrasena por email.
- Reset con token temporal.
- Login con nueva contrasena.
- Generacion de notificacion por moderacion.
- Lectura de notificacion por el usuario afectado.
- Generacion de notificaciones por matching, contraoferta recibida y contraoferta resuelta.
- Validacion de endpoints protegidos por usuario/admin.
- Chat asociado a solicitud con participantes autorizados.
- Comentario de calificacion persistido y recuperable.
- Eliminacion individual de notificaciones.
- Categorias administrables desde panel admin.
- Filtros temporales de historial.
- Perfil publico mas liviano con servicios activos bajo demanda.

Limitacion:

- No se dejo servidor local levantado para validacion manual prolongada, por pedido de trabajar solamente mediante Docker para pruebas de usuario.

## 14. Estado de Jira asociado

Actualizaciones relevantes realizadas en el tablero:

- `SVF-90 - Recuperar contrasena`: implementada localmente y movida a revision.
- `SVF-93 - Notificaciones basicas`: implementada localmente y movida a revision.
- `SVF-8 - Product Backlog del MVP`: comentado con el avance de `SVF-90` y `SVF-93`.

Tambien quedaron alineadas con este sprint las mejoras administrativas, de perfil publico, busqueda de prestadores, modo oscuro, barra lateral y categorias.

Actualizacion 17 de junio de 2026:

- Se sincronizo Jira mediante Atlassian Rovo remoto.
- Se agrego comentario de actualizacion Sprint 3 en `SVF-8 - Product Backlog del MVP`.
- Se crearon `SVF-137`, `SVF-138`, `SVF-139` y `SVF-140`.
- Se movieron a `En revision` las US implementadas que seguian abiertas o nuevas: `SVF-92`, `SVF-97`, `SVF-99`, `SVF-137`, `SVF-138` y `SVF-139`.
- Se mantuvo `SVF-140` como `Pendiente` por dependencia tecnica de modelo relacional y matching.
- Se agregaron comentarios de trazabilidad en `SVF-90`, `SVF-92`, `SVF-93`, `SVF-94`, `SVF-97`, `SVF-99`, `SVF-100` y `SVF-136`.

User stories Sprint 3 a marcar como implementadas o listas para revision:

- Recuperacion de contrasena por email registrado.
- Notificaciones internas persistidas para eventos de solicitudes, moderacion, calificaciones y chat.
- Instalacion PWA de Servify en mobile.
- Flujo de contraoferta consistente con aceptar precio o seguir buscando.
- Chat interno asociado a solicitud.
- Comentarios de calificacion persistidos y visibles en perfil publico.
- Perfil publico liviano con servicios activos bajo demanda.
- Historial de solicitudes/servicios con filtros temporales.
- Administracion de categorias desde el panel admin.

User story a mantener como pendiente tecnica:

- `SVF-140 - Publicaciones/solicitudes con multiples categorias`. Motivo: requiere cambio de modelo relacional, migracion y ajuste del matching para trabajar por conjuntos de categorias.

## 15. Resultado funcional del Sprint 3

Al cierre de este sprint, Servify no solo permite demostrar el flujo central del marketplace, sino tambien:

1. Recuperar acceso a una cuenta.
2. Usar nombre de usuario como identificador publico y de login.
3. Buscar prestadores por usuario.
4. Entrar a perfiles publicos.
5. Repetir solicitudes.
6. Navegar con una barra inferior mas estable.
7. Configurar cuenta, privacidad, apariencia y notificaciones.
8. Usar la app en modo oscuro.
9. Moderar usuarios y publicaciones desde una cuenta admin.
10. Notificar al usuario afectado por acciones administrativas.
11. Versionar cambios de base con Flyway.
12. Instalar la app como PWA desde Android/iOS.
13. Conversar por chat dentro de una solicitud.
14. Persistir comentarios de calificaciones y mostrarlos en perfiles publicos.
15. Filtrar historiales para evitar pantallas cargadas.
16. Administrar categorias sin tocar base de datos manualmente.

## 16. Funcionalidades criticas que siguen

Quedan como proximos avances sugeridos:

- `SVF-100 - Reportes y denuncias`.
- `SVF-136 - Auditar acciones de moderacion administrativa`.
- Push notifications reales mediante Web Push o servicio nativo.
- Chat por WebSocket/SSE si se necesita tiempo real estricto.
- Publicaciones/solicitudes con multiples categorias.
- Mejoras de metricas administrativas y reportes operativos.

La siguiente prioridad natural sigue siendo reportes/denuncias, porque complementa moderacion y permite que usuarios comuniquen contenido inseguro o inapropiado sin depender solo de revision manual del admin.
