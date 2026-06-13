# Informe de avances del sprint: conexion Front + Back + BD

## 1. Resumen ejecutivo

Durante este sprint se avanzo fuertemente en la integracion real del MVP de Servify. El frontend en React ya se conecta con el backend Spring Boot, y el backend persiste informacion en PostgreSQL. Esto nos permitio dejar de probar solamente pantallas aisladas o datos simulados, y empezar a validar el flujo completo de negocio con datos reales.

El objetivo principal fue que una persona pueda:

1. Registrarse o iniciar sesion.
2. Crear una publicacion como prestador.
3. Crear una solicitud como cliente.
4. Hacer que el motor de matching encuentre compatibilidad.
5. Permitir que el prestador acepte, rechace o contraoferte.
6. Permitir que el solicitante confirme al prestador.
7. Finalizar el servicio con confirmacion de ambas partes.
8. Calificar la experiencia.

El flujo principal ya esta conectado de punta a punta, con persistencia en base de datos y pruebas automatizadas que validan el comportamiento.

## 2. Arquitectura actual del MVP

La aplicacion quedo organizada en tres partes principales:

- Frontend: aplicacion React.
- Backend: API REST construida con Spring Boot.
- Base de datos: PostgreSQL real para desarrollo local y pruebas manuales.

Tambien se mantiene H2 para los tests automatizados. Esto permite que la suite corra rapido y sin depender de una base externa.

En Docker, los servicios quedan asi:

- Frontend: `http://localhost:5173`
- Backend: expuesto localmente en `http://localhost:8082`
- Base PostgreSQL: `localhost:5432`

Cuando se usa Docker, el frontend llama a la API usando `/api/v1`. Nginx redirige esas llamadas al backend internamente, por lo que desde el navegador alcanza con abrir el frontend.

## 3. Conexion entre frontend y backend

Se conectaron las pantallas principales del frontend con endpoints reales del backend. Ya no se depende solamente de datos mockeados para el flujo MVP.

Las conexiones principales son:

- Autenticacion: registro, login tradicional y login social.
- Usuarios/perfil: consulta y actualizacion de datos del usuario.
- Publicaciones: crear, listar, editar, pausar y eliminar logicamente.
- Solicitudes: crear solicitudes y listar las solicitudes del usuario actual.
- Matching: distribucion de solicitudes compatibles a prestadores.
- Respuestas del prestador: aceptar, rechazar o contraofertar.
- Asignacion: confirmacion del prestador aceptado por parte del solicitante.
- Finalizacion: confirmacion de cierre por solicitante y prestador.
- Calificaciones: valoracion posterior al servicio.

Tambien se corrigieron problemas de CORS para permitir pruebas desde entorno local, Expo, LAN y navegador.

## 4. Conexion con PostgreSQL

El backend ya trabaja contra PostgreSQL para la ejecucion local con Docker. La base persiste los datos importantes del flujo MVP:

- Usuarios.
- Perfiles.
- Credenciales.
- Identidades externas para login social.
- Categorias.
- Publicaciones.
- Zonas de cobertura de publicaciones.
- Solicitudes.
- Distribuciones generadas por el motor de matching.
- Contraofertas.
- Asignaciones.
- Confirmaciones de finalizacion.
- Calificaciones.

Se normalizo la forma de guardar zonas de cobertura para que una publicacion pueda tener varias zonas sin duplicar publicaciones. Esto era importante para que un prestador pueda ofrecer el mismo servicio en varias localidades sin crear registros repetidos.

Tambien se agrego soporte para calificaciones bidireccionales. Ahora una misma asignacion puede recibir:

- Una calificacion del solicitante al prestador.
- Una calificacion del prestador al solicitante.

Esto deja mejor preparado el sistema para construir reputacion de ambos lados.

## 5. Publicaciones

Se pulio bastante la pantalla y el comportamiento de publicaciones.

Cambios principales:

- El prestador puede crear una publicacion real y verla persistida en la BD.
- Se agrego edicion de publicaciones.
- Se agrego pausa/reactivacion.
- Se agrego eliminacion logica.
- Se corrigio la creacion con multiples zonas para que sea una sola publicacion con varias zonas, no varias publicaciones duplicadas.
- Se adapto la visualizacion de publicaciones por categoria.
- Se corrigio el flujo para que entrar a una categoria no redirija a crear publicacion, sino que muestre publicaciones disponibles.

Esto mejora mucho la demo porque permite mostrar un prestador gestionando su oferta de servicios.

## 6. Solicitudes y matching

El motor de matching es el punto central del MVP. Durante el sprint se corrigieron varios problemas para que la solicitud creada por un cliente llegue al prestador correcto.

El matching considera principalmente:

- Categoria del servicio.
- Zona/localidad.
- Disponibilidad horaria.
- Modalidad del servicio.
- Estado activo de la publicacion.

Tambien se corrigio un problema importante: el precio de referencia de la solicitud estaba actuando como filtro demasiado estricto. Por ejemplo, si el cliente ponia un presupuesto menor al precio base de la publicacion, el match no aparecia. Ahora ese precio no bloquea el match, porque el prestador puede aceptar o contraofertar.

La pantalla de solicitudes tambien fue ajustada:

- El solicitante ve sus propias solicitudes.
- El prestador ve solicitudes recibidas compatibles.
- El prestador puede aceptar, rechazar o contraofertar.
- Se dejo de mostrar informacion global que no correspondia al usuario actual.

## 7. Flujo de aceptacion, asignacion y finalizacion

Se detecto y corrigio un punto clave del flujo.

Antes, cuando el prestador aceptaba una solicitud, el backend marcaba la distribucion como aceptada, pero la solicitud seguia figurando como abierta para el solicitante. Tecnicamente no era un error total del backend: faltaba que el solicitante confirmara a ese prestador para crear la asignacion final.

Lo que se corrigio:

- El backend ahora expone las distribuciones aceptadas en el estado de asignacion.
- El frontend detecta cuando un prestador acepto una solicitud.
- El solicitante ve un boton para confirmar al prestador.
- Al confirmar, se crea la asignacion y la solicitud pasa a estar en curso.
- Ambos roles pueden confirmar finalizacion.
- El servicio solo queda finalizado cuando confirman solicitante y prestador.

Este flujo protege a ambas partes y representa mejor una operacion real de marketplace.

## 8. Calificaciones y reputacion

Se conecto la calificacion al backend real. Antes el modal podia mostrarse visualmente, pero no persistia correctamente en la base.

Ahora:

- El solicitante puede calificar al prestador.
- El prestador puede calificar al solicitante.
- La reputacion se calcula con cantidad de valoraciones y promedio de estrellas.
- La calificacion queda guardada en PostgreSQL.

Esto permite mostrar en la presentacion que Servify no solo conecta usuarios, sino que empieza a construir confianza dentro de la comunidad.

## 9. Autenticacion

Se avanzo con autenticacion tradicional y social.

Funcionalidades trabajadas:

- Registro con email y password.
- Inicio de sesion tradicional.
- Validaciones de formulario en frontend.
- Password visible/oculta en registro.
- Confirmacion de password.
- Login con Google usando ID Token OIDC.
- Endpoint preparado para LinkedIn.
- Configuracion por variables de entorno.

Tambien se resolvieron errores reales durante la integracion:

- Error de CORS al registrar o iniciar sesion.
- Error de Google `invalid_client`.
- Error de audiencia OIDC incorrecta.
- Configuracion de client IDs para entorno local.

El login social ya queda preparado de forma mas profesional: el frontend obtiene el token del proveedor y el backend valida que sea autentico antes de iniciar sesion o crear usuario.

## 10. Perfil de usuario

Se hicieron mejoras utiles para el flujo del MVP:

- El perfil permite seleccionar imagen desde la fototeca.
- Se agrego soporte para camara.
- Se muestra reputacion con cantidad de valoraciones y promedio.
- Se permite cambiar el tipo de cuenta si el usuario se equivoco o cambia de rol.
- Se mejoro el nombre del usuario creado con Google para que no use solamente el email.

Estas mejoras hacen que la demo se vea mas completa y menos como una prueba tecnica aislada.

## 11. Notificaciones internas

No se implementaron notificaciones push para este release. En su lugar, se aprovecho la campanita del inicio para mostrar informacion util con datos ya disponibles del backend.

La idea de esta primera version es mostrar actividad relevante dentro de la app, por ejemplo:

- Solicitudes compatibles.
- Solicitudes aceptadas.
- Estados pendientes de accion.

Esto permite tener una experiencia de notificaciones basica sin incorporar todavia servicios externos de push notifications.

## 12. Docker y entorno local

Se agrego configuracion para levantar frontend, backend y base de datos juntos mediante Docker Compose.

Esto permite que el equipo pueda probar el MVP de forma mas ordenada, sin tener que levantar cada parte manualmente.

Servicios incluidos:

- PostgreSQL.
- Backend Spring Boot.
- Frontend React servido con Nginx.

El frontend en Docker usa `/api/v1` y Nginx se encarga de redirigir al backend. Esto simplifica las pruebas desde navegador.

## 13. Correcciones recientes destacadas

Principales bugs corregidos desde la conexion front-back:

- Error de CORS al registrar e iniciar sesion.
- Problemas de configuracion de Google OAuth.
- Solicitudes compatibles que no aparecian por filtro de precio demasiado estricto.
- Publicaciones con multiples zonas que se creaban duplicadas.
- Pantalla de solicitudes mostrando datos que no correspondian al usuario actual.
- Solicitudes aceptadas por prestador que seguian apareciendo como abiertas sin siguiente accion.
- Falta de boton para confirmar prestador desde el lado solicitante.
- Falta de finalizacion clara por ambos roles.
- Calificacion visual sin persistencia real.
- Validacion de arranque con PostgreSQL y parches incrementales de BD.

## 14. Pruebas realizadas

Se mantuvo y amplio la suite automatizada del MVP.

Actualmente se validan, entre otros puntos:

- Creacion de usuarios.
- Login social con proveedor fake para tests.
- Creacion de publicaciones.
- Busqueda por categoria.
- Matching entre solicitud y publicacion compatible.
- Matching sin bloquear por precio menor.
- Respuesta del prestador.
- Confirmacion de asignacion.
- Confirmacion de finalizacion por ambas partes.
- Calificacion por solicitante y prestador.
- Reputacion resultante.
- CORS para entornos locales.
- Eliminacion logica de publicaciones.

Resultado reciente:

```text
Tests run: 17
Failures: 0
Errors: 0
Skipped: 0
Build success
```

Tambien se verifico que el frontend compile correctamente y que los contenedores respondan.

## 15. Estado actual del MVP

El MVP ya permite demostrar el ciclo central de Servify:

1. Un prestador publica un servicio.
2. Un cliente crea una solicitud.
3. El sistema encuentra compatibilidad.
4. El prestador responde.
5. El cliente confirma al prestador.
6. El servicio queda asignado.
7. Ambas partes finalizan.
8. Ambas partes pueden calificar.

Quedan pendientes futuras mejoras, pero el flujo principal ya esta integrado y es demostrable con datos persistidos.

## 16. Ideas para presentar el avance del sprint

Para una presentacion clara, se recomienda mostrarlo como una historia:

1. "Antes teniamos pantallas y backend por separado."
2. "Ahora el frontend habla con la API real."
3. "La informacion se guarda en PostgreSQL."
4. "El motor de matching ya conecta solicitudes con publicaciones compatibles."
5. "El prestador puede responder."
6. "El solicitante confirma la asignacion."
7. "Ambas partes finalizan y califican."

Mensaje clave para la presentacion:

> En este sprint pasamos de una integracion parcial a un flujo MVP conectado de punta a punta, con persistencia real, matching funcional y acciones completas para solicitante y prestador.
