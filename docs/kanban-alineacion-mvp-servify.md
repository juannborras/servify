# Alineacion Kanban y roadmap MVP - Servify

Fecha de analisis: 2026-06-11

## Evidencia revisada y ejecutada

- Backlog MVP/Release 2/Release 3: `Entrega jueves 17-05/Servify Backlog.xlsx`.
- Plan/Kanban exportado: `Entrega jueves 17-05/Plan de trabajo Servify.xlsx`.
- Codigo backend/frontend en `Servify`.
- Tests ejecutados: `.\mvnw.cmd test`.
- App local levantada:
  - PostgreSQL real con Docker Compose: `servify_db`, DB `Servify`, puerto `5432`.
  - Backend Spring Boot: `http://localhost:8080/api/v1`.
  - Frontend Vite: `http://localhost:5173`.
- Prueba E2E ejecutada pasando por el proxy del frontend: `http://localhost:5173/api/v1`.

Resultado de tests: 24 tests, 0 fallos, build exitoso.

Resultado de build frontend: `npm run frontend:build` exitoso.

Resultado de persistencia SQL: la prueba E2E creo y persistio usuarios, credenciales, perfiles, categoria, publicacion, solicitud finalizada, solicitud cancelada, asignacion finalizada y dos calificaciones en PostgreSQL local.

## Funcionalidades cubiertas

### MVP cubierto en backend + tests

- US01 Cuenta: registro de usuario.
- US02 Acceso: login por credenciales, refresh/logout y login social Google/LinkedIn.
- US03 Perfil: completar, editar y consultar perfil.
- US04 Publicaciones: crear publicaciones con categoria, modalidad, zonas, disponibilidad y precio base.
- US05 Solicitudes: crear solicitudes de servicio.
- US06 Matching: busqueda/distribucion por compatibilidad, disponibilidad, zona, modalidad y relevancia.
- US07 Respuestas: aceptar, rechazar y contraofertar solicitudes recibidas.
- US08 Asignacion: confirmacion de asignacion unica.
- US09 Cierre: confirmacion de finalizacion por ambas partes.
- US10 Reputacion: calificacion de servicio finalizado y resumen de reputacion.
- US11 Categorias: alta, activacion/desactivacion y listado.
- US12 Publicaciones editables: edicion de publicaciones existentes.
- US13 Estado de publicaciones: activar, pausar/desactivar y borrado logico.
- US14 Estado de solicitud: consulta de estado de asignacion, distribuciones aceptadas y contraofertas.
- US15 Cancelacion: cancelar/eliminar solicitud activa.
- US16 Solicitudes recibidas: listado para prestadores.

### Release 2 parcialmente adelantado

- US18 Historial solicitante: cubierto como listado de solicitudes del usuario.
- US19 Historial prestador: cubierto parcialmente como solicitudes recibidas/servicios asociados; faltaria vista historica final mas clara.
- US21 Perfil publico prestador: parcialmente cubierto por perfil, reputacion y publicaciones de usuario; falta endpoint/vista publica consolidada.
- US22 Moderacion: backend administrativo para moderar publicaciones.
- US23 Usuarios: backend permite cambiar estado de usuario; falta flujo admin completo y medidas visibles.

### Release 3 parcialmente adelantado

- US26 Resenas: existe calificacion y consulta, pero no comentario escrito moderable.
- US28 Identidad: existe login social/verificacion OIDC, pero no verificacion avanzada documental.
- US31 Analitica: hay configuracion/admin basico; faltan metricas avanzadas.

## Actualizacion Kanban ejecutada en Jira

### Movido a Finalizado

- SVF-74 US01 - Registrarse en la plataforma.
- SVF-83 US10 - Calificar servicio.
- SVF-84 US11 - Gestionar categorias.
- SVF-85 US12 - Editar publicacion de servicio.
- SVF-89 US16 - Visualizar solicitudes recibidas.
- SVF-106 Finalizar modulo usuarios.
- SVF-107 Finalizar modulo Publicaciones.
- SVF-108 Finalizar modulo Solicitudes.
- SVF-109 Finalizar modulo Administracion, al menos como backend MVP.
- SVF-110 Finalizar modulo Autenticacion.
- SVF-117 Identificar entidades principales y sus atributos.
- SVF-118 Definir relaciones entre entidades.
- SVF-119 Documentar el modelo relacional inicial.

### Movido a En revision

- SVF-66 Disenar flujo de navegacion principal de la app movil.
- SVF-69 Disenar wireframes de creacion de publicacion.
- SVF-95 US22 - Moderar publicaciones.
- SVF-96 US23 - Bloquear o suspender usuarios.

Justificacion: la app React ya tiene navegacion principal y flujo de publicacion representado, pero conviene revisar visualmente la demo antes de cerrar esas tareas de UX. Moderacion y bloqueo/suspension tienen backend funcional, pero falta validar flujo administrativo completo/UI.

### Verificacion Jira posterior

- Consulta JQL ejecutada: `project = SVF AND labels = mvp AND status != Finalizado ORDER BY key ASC`.
- Resultado: solo queda abierto `SVF-112 Estimar costos operativos del MVP`, en estado `En curso`.

### Mantener Pendiente o En curso

- SVF-59 Definir agenda de Daily Scrums.
- SVF-60 Preparar presentacion de entrega de 7 minutos.
- SVF-61 Ensayar presentacion oral de 7 minutos.
- SVF-62 Tomar capturas del tablero Kanban y backlog.
- SVF-63 Validar entregables finales.
- SVF-64 Disenar identidad visual base de Servify.
- SVF-65 Definir paleta de colores, tipografias e iconografia.
- SVF-112 Estimar costos operativos del MVP.
- SVF-113 Definir estrategia de monetizacion inicial.
- SVF-114 Definir metricas de validacion comercial.
- SVF-115 Preparar resumen financiero.

## Pruebas locales ejecutadas el 2026-06-11

### Infraestructura local

- `docker compose up -d db`: PostgreSQL local iniciado y saludable.
- Spring Boot arranco correctamente con `jdbc:postgresql://localhost:5432/Servify`.
- Vite arranco correctamente en `http://localhost:5173`.
- El frontend usa proxy `/api` hacia `http://localhost:8080`, por lo que la prueba E2E uso la misma ruta de integracion que la app en desarrollo.

### Flujo E2E validado contra PostgreSQL

- Registro de cliente y prestador.
- Creacion de credenciales y login por email/password.
- Perfil completo para ambos usuarios.
- Creacion y activacion de categoria.
- Creacion, edicion y activacion de publicacion.
- Listado de publicaciones por categoria.
- Creacion de solicitud compatible.
- Distribucion de solicitud al prestador.
- Aceptacion de solicitud recibida.
- Confirmacion de asignacion por solicitante.
- Confirmacion de finalizacion por solicitante y prestador.
- Calificacion del prestador al solicitante y del solicitante al prestador.
- Creacion y cancelacion de una segunda solicitud.

### Evidencia SQL de persistencia

- Usuarios E2E persistidos con `perfil_completo = true`.
- Categoria E2E persistida con `activa = true`.
- Publicacion E2E persistida con `estado = activa`.
- Solicitud principal E2E persistida con `estado = finalizada`.
- Solicitud secundaria E2E persistida con `estado = cancelada`.
- Asignacion E2E persistida con `estado = finalizada` y `precio_acordado = 15000.00`.
- Calificaciones E2E persistidas:
  - Rol `solicitante`, puntaje 5.
  - Rol `prestador`, puntaje 4.

### Resultado de regresion automatica

- Backend: `.\mvnw.cmd test` -> 24 tests, 0 fallos, 0 errores.
- Frontend: `npm run frontend:build` -> build Vite exitoso.

## Errores y riesgos detectados

- No se detectaron fallos funcionales en el flujo E2E local contra PostgreSQL.
- No se detectaron errores de build frontend.
- Warnings backend:
  - `PostgreSQLDialect does not need to be specified explicitly`: limpiar `spring.jpa.database-platform` en algun refactor futuro.
  - `spring.jpa.open-in-view is enabled by default`: conviene definir `spring.jpa.open-in-view=false` para produccion si no dependen de lazy loading en web.
- Warnings tests/JDK:
  - Mockito esta usando self-attach del inline mock maker; futuras versiones del JDK pueden requerir configurar Mockito como Java agent.
- Limitacion de esta corrida:
  - No pude automatizar clicks reales en navegador por bloqueo del sandbox sobre la herramienta Browser/node_repl. La validacion se hizo pasando por el proxy local del frontend y los mismos endpoints que consume la app, mas verificacion SQL directa.

## Administracion de base de datos en Railway

- Railway crea un servicio PostgreSQL con variables disponibles como `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` y `DATABASE_URL`.
- Para que el backend Spring Boot use Railway correctamente, mapear:
  - `SPRING_DATASOURCE_URL` a una URL JDBC de Postgres.
  - `SPRING_DATASOURCE_USERNAME` al usuario Railway.
  - `SPRING_DATASOURCE_PASSWORD` al password Railway.
- Si el backend y Postgres estan en el mismo proyecto/entorno de Railway, conviene usar comunicacion privada entre servicios en vez de exponer la DB publicamente.
- Para administrar datos:
  - Usar la Database View de Railway para ver tablas y editar entradas simples.
  - Usar la pestana Credentials para regenerar password sin desincronizar variables.
  - Usar un cliente externo como DBeaver/TablePlus/pgAdmin solo si habilitan/usan TCP Proxy, sabiendo que cuenta como egress.
- Para produccion:
  - Activar backups.
  - Evitar cambios manuales de schema desde la UI; usar migraciones/versionado cuando el proyecto crezca.
  - Revisar logs y metricas del servicio Postgres y del backend despues de cada deploy.

## Orden recomendado de pruebas restantes

1. Smoke tecnico: levantar PostgreSQL con Docker, backend y frontend; validar que la app abre y que `/api/v1/usuarios` responde.
2. Auth y sesion: registro, login, refresh/logout, persistencia de sesion frontend y login social en modo configurado/fake.
3. Perfil: completar perfil, editar datos, verificar que habilita crear publicaciones.
4. Categorias y publicaciones: crear categoria, crear publicacion, activar/pausar, editar, listar por categoria y por usuario.
5. Solicitud/matching: crear solicitud compatible, validar distribucion, priorizacion por disponibilidad/zona/servicio y reintento de distribucion.
6. Prestador: listar solicitudes recibidas, aceptar, rechazar y contraofertar.
7. Solicitante: resolver contraoferta, confirmar asignacion, ver estado de solicitud.
8. Cierre: confirmar finalizacion por ambas partes.
9. Reputacion: calificar una vez, consultar calificacion y reputacion.
10. Administracion: moderar publicacion, cambiar estado de usuario, revisar configuracion general.
11. Regresion negativa: publicar con perfil incompleto, cancelar solicitud finalizada, calificar dos veces, aceptar dos prestadores para la misma solicitud, usar publicacion inactiva en matching.
12. UX demo: recorrer el flujo completo desde la app React, grabar/capturar pantallas para la entrega.

## Release futuro que conviene adelantar

- Recuperacion de password: alto valor, bajo riesgo si se implementa con token temporal fake/local primero.
- Perfil publico consolidado del prestador: reutiliza perfil + reputacion + publicaciones; mejora confianza y demo.
- Historial claro para solicitante/prestador: ya hay endpoints base; falta separar vistas y estados finales.
- Notificaciones basicas in-app: empezar con centro de notificaciones persistido o badges locales, sin push real.
- Reportes/denuncias simples: tabla + endpoint + vista admin; prepara seguridad comunitaria.
- Comentarios escritos en calificacion: extension natural de reputacion, sin integrar pagos/chat.
- Metricas admin basicas: conteos de usuarios, publicaciones, solicitudes por estado y conversion de matching.

## User stories sugeridas

- Como usuario, quiero guardar la localidad y preferencias de disponibilidad para no repetirlas en cada solicitud o publicacion.
- Como prestador, quiero definir multiples zonas de cobertura por publicacion para recibir solicitudes relevantes.
- Como solicitante, quiero ver por que una solicitud esta sin prestador para ajustar disponibilidad, zona o presupuesto.
- Como usuario, quiero recibir confirmaciones visuales de cada cambio de estado para entender el avance del servicio.
- Como administrador, quiero ver un tablero operativo basico con publicaciones bloqueadas, usuarios suspendidos y solicitudes sin asignar.
- Como equipo, quiero un flujo seed/demo reproducible para mostrar el MVP con datos consistentes en cada entrega.

## Acceso a Jira para actualizarlo

No hay conector Jira instalado en esta sesion. Opciones:

1. API de Jira Cloud: compartir URL del sitio, project key, email de la cuenta y API token. Lo ideal es pasarlo como variables de entorno o secreto temporal, no pegarlo en texto plano.
2. CSV import: puedo generar un CSV con `Issue key`, `Summary`, `Status`, `Labels`, `Fix version` y `Comment` para que lo importen desde Jira.
3. Export/import manual: si exportan el tablero actual a CSV/XLSX, puedo devolverles una version actualizada para reimportar o usar como checklist de cambios.

Para cambios directos por API necesito permiso de edicion de issues/transiciones en el proyecto Jira y saber los nombres exactos de columnas/status del tablero.
