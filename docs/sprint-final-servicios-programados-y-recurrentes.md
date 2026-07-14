# Sprint final - Servicios programados y recurrentes

Fecha de inicio: 29 de junio de 2026

## Objetivo

Permitir que Servify centralice la coordinacion completa del servicio dentro de la app:

- solicitudes para una fecha y horario futuros;
- nuevos encuentros dentro de una solicitud ya asignada;
- servicios recurrentes aceptados por prestadores;
- cancelacion trazable y notificada entre solicitante y prestador.

## Estado Jira

Intento de actualizacion desde Codex: bloqueado.

Resultado del conector Atlassian Rovo:

```text
403 - The app is not installed on this instance
```

Cuando el conector quede instalado/autorizado, crear o actualizar el tablero Kanban con la estructura de backlog de este documento.

## Epica propuesta

**Servicios programados y recurrentes**

Implementar la capacidad de programar servicios futuros, coordinar encuentros adicionales y gestionar servicios recurrentes con notificaciones, trazabilidad y cancelacion bilateral.

## Historias del sprint final

### Historia 1 - Solicitud programada unica

Como solicitante, quiero crear una solicitud para una fecha y horario concretos, para coordinar un servicio futuro sin depender de que sea inmediato.

**Criterios de aceptacion**

- El solicitante puede elegir entre solicitud inmediata y solicitud programada.
- La solicitud programada guarda fecha/hora de inicio y fin.
- El motor de matching usa el dia de semana y horario programado para comparar contra la disponibilidad del prestador.
- La fecha programada se muestra en el detalle y en la lista de solicitudes.
- La solicitud conserva compatibilidad con el flujo actual de aceptacion, contraoferta, asignacion, chat, finalizacion y calificacion.

### Historia 2 - Nuevo encuentro dentro de una solicitud asignada

Como solicitante o prestador, quiero proponer un nuevo encuentro dentro de una solicitud en curso, para continuar el servicio sin crear una solicitud nueva.

**Criterios de aceptacion**

- Cualquier participante de una solicitud asignada puede proponer un encuentro.
- El otro participante puede aceptar o rechazar el encuentro.
- Los encuentros quedan listados en el detalle de la solicitud.
- Un encuentro puede cancelarse sin borrar historial.
- Las acciones relevantes notifican a la contraparte.

### Historia 3 - Servicio recurrente

Como solicitante, quiero solicitar un servicio recurrente indicando frecuencia, fecha, horario y condiciones, para que un prestador acepte un compromiso repetido.

**Criterios de aceptacion**

- El solicitante puede crear una solicitud recurrente semanal, quincenal o mensual.
- La recurrencia define dia, hora desde, hora hasta, fecha de inicio y fecha de fin opcional.
- El prestador ve que la solicitud implica recurrencia antes de aceptarla.
- Al confirmarse la asignacion, la recurrencia queda activa.
- La recurrencia puede cancelarse por cualquiera de las partes con confirmacion visual y notificacion.

## Backlog tecnico propuesto para Jira

1. **Story** - Crear solicitudes programadas con fecha y horario concretos.
2. **Story** - Gestionar encuentros adicionales dentro de una solicitud asignada.
3. **Story** - Crear y activar servicios recurrentes.
4. **Task** - Agregar migracion PostgreSQL para programacion, encuentros y recurrencias.
5. **Task** - Extender endpoints, DTOs, JPA adapters y servicios de aplicacion.
6. **Task** - Integrar notificaciones para propuestas, resoluciones y cancelaciones.
7. **Story** - Actualizar frontend de creacion y detalle de solicitudes.
8. **Task** - Agregar pruebas del flujo programado, encuentros y recurrencia.

## Decisiones tecnicas

- La fecha de creacion de solicitud no representa la fecha del servicio.
- La solicitud conserva el flujo principal de oferta/demanda.
- Los encuentros representan fechas concretas del servicio.
- La recurrencia representa una regla de repeticion, no una lista infinita de solicitudes clonadas.
- La cancelacion mantiene trazabilidad y no elimina datos operativos.

## Plan de implementacion

- Agregar campos de programacion a `solicitud_servicio`.
- Crear tabla `servicio_encuentro`.
- Crear tabla `servicio_recurrencia`.
- Extender dominio y DTOs de solicitudes.
- Agregar endpoints para listar, proponer, resolver y cancelar encuentros.
- Agregar endpoints para cancelar recurrencias.
- Adaptar frontend para elegir tipo de programacion y gestionar encuentros desde el detalle.
- Verificar con tests automatizados y flujo local.

## Implementacion realizada

### Backend

- Se agrego la migracion `V16__servicios_programados_y_recurrentes.sql`.
- `solicitud_servicio` ahora guarda `tipo_programacion`, `fecha_programada_inicio` y `fecha_programada_fin`.
- Se agregaron las tablas `servicio_encuentro` y `servicio_recurrencia`.
- Se extendio el dominio con:
  - `SolicitudServicio` con tipo de programacion.
  - `ServicioEncuentro`.
  - `ServicioRecurrencia`.
- Se agregaron enums de estado/frecuencia:
  - `TipoProgramacionSolicitud`.
  - `EstadoEncuentroServicio`.
  - `EstadoRecurrenciaServicio`.
  - `FrecuenciaRecurrencia`.
- Se agregaron puertos, DTOs, servicios de aplicacion y adapters JPA para encuentros y recurrencias.
- Al confirmar una asignacion:
  - si la solicitud era programada, se crea un encuentro confirmado;
  - si era recurrente, se activa la recurrencia y se crea el primer encuentro confirmado.
- Se agregaron notificaciones para:
  - encuentro propuesto;
  - encuentro resuelto;
  - encuentro cancelado;
  - recurrencia cancelada.

### Endpoints nuevos

```text
GET    /api/v1/solicitudes/{solicitudId}/encuentros
POST   /api/v1/solicitudes/{solicitudId}/encuentros
POST   /api/v1/encuentros/{encuentroId}/resoluciones
DELETE /api/v1/encuentros/{encuentroId}
GET    /api/v1/solicitudes/{solicitudId}/recurrencia
DELETE /api/v1/solicitudes/{solicitudId}/recurrencia
```

### Frontend

- El modal de nueva solicitud permite elegir:
  - inmediata;
  - programada;
  - recurrente.
- Las solicitudes programadas envian fecha/hora concreta al backend.
- Las solicitudes recurrentes envian frecuencia, fecha de inicio y fecha de fin opcional.
- La lista de solicitudes muestra si el pedido es programado o recurrente.
- El detalle de solicitud muestra la agenda del servicio.
- Desde el detalle se puede:
  - ver encuentros;
  - proponer un nuevo encuentro;
  - aceptar o rechazar un encuentro propuesto;
  - cancelar un encuentro;
  - cancelar una recurrencia activa.

### Pulido UX - recurrencias y Servis

El 3 de julio de 2026 se ajusto el flujo visual de solicitudes recurrentes:

- La fecha de fin de una recurrencia queda como opcional para cubrir servicios sin periodo cerrado.
- La creacion muestra un resumen claro del compromiso: `Encuentros semanales/quincenales/mensuales desde fecha hasta fecha` o `sin fecha final`.
- Para solicitudes programadas y recurrentes, el dia de semana se calcula desde la fecha elegida; el usuario solo elige fecha y horario.
- El detalle de solicitud muestra una tarjeta de recurrencia con frecuencia, ventana de fechas, estado y accion de cancelacion.
- Se incorporo el personaje de marca Servis como apoyo visual en la creacion y en el detalle, sin tapar acciones ni informacion operativa.

### Pulido UX - segunda visita

El 7 de julio de 2026 se aclaro el acceso a la programacion de una segunda visita desde el detalle de solicitud:

- La accion ahora aparece como `Programar segunda visita` dentro de `Agenda del servicio`.
- Si la solicitud todavia no tiene prestador confirmado, la pantalla explica que la segunda visita se habilita cuando el solicitante confirma al prestador y la solicitud queda en curso.
- El formulario valida que la hora de fin sea posterior a la hora de inicio antes de permitir proponer el encuentro.
- La propuesta sigue usando el endpoint `POST /api/v1/solicitudes/{solicitudId}/encuentros` y conserva el historial sin crear una solicitud nueva.

### Pulido UX - agenda del prestador

El 7 de julio de 2026 se agrego una seccion de organizacion para prestadores dentro de `Mis servicios`.

**Objetivo funcional**

Separar la organizacion futura del prestador del historial de publicaciones y solicitudes. La nueva pestaña `Agenda` muestra solamente trabajos futuros asociados a solicitudes ya asignadas, incluyendo servicios programados, recurrentes y segundas visitas confirmadas o propuestas.

**Como funciona en la app**

1. El prestador entra a `Mis servicios`.
2. La pantalla abre por defecto en la pestaña `Agenda`.
3. Cada item muestra:
   - servicio;
   - categoria;
   - solicitante;
   - dia;
   - horario;
   - precio acordado o de referencia;
   - estado del encuentro.
4. `Contactar` abre el detalle de la solicitud para continuar el seguimiento y el chat.
5. `Cancelar` abre una confirmacion visual con Servis recomendando avisarle al solicitante antes de cancelar.
6. La cancelacion usa `DELETE /api/v1/encuentros/{encuentroId}` y conserva trazabilidad; no elimina solicitudes ni historial.

**Criterio de visibilidad**

La agenda lista encuentros futuros con estado `CONFIRMADO` o `PROPUESTO`, siempre que la asignacion siga activa y corresponda al prestador logueado. Tambien representa recurrencias activas como compromiso futuro cuando todavia no hay un encuentro concreto generado para la proxima fecha. Los encuentros finalizados, cancelados o de solicitudes cerradas quedan fuera de esta vista operativa y se mantienen en el historial de solicitudes.

### Pulido visual - Servis como guia

El 7 de julio de 2026 se ajusto la presencia de Servis para que no aparezca como una imagen pegada dentro de una tarjeta.

- Se agrego `ServisHint` como patron visual reutilizable con globo de texto.
- Servis se renderiza como personaje de apoyo junto al mensaje, con poses CSS (`coach`, `wave`, `peek`) y sin recuadro propio.
- La idea es que aparezca en estados vacios, confirmaciones y ayudas puntuales, sin competir con las acciones principales.

### Correccion de estabilidad del detalle

El 2 de julio de 2026 se agrego una segunda correccion sobre el mismo flujo: si el backend devuelve una respuesta vacia para los encuentros de una solicitud, el frontend ahora la interpreta como una lista vacia y no como `undefined`.

**Causa detectada**

El contenedor ya estaba sirviendo una build nueva, por lo que el problema no era una version vieja del frontend. La falla mas probable estaba en `/solicitudes/{id}/encuentros`: cuando no habia encuentros programados, una respuesta sin cuerpo podia llegar al detalle como `undefined`. Luego `RequestDetail` intentaba leer `encounters.length` y React rompia el render completo.

**Solucion aplicada**

- `servifyApi.listServiceEncounters` normaliza respuestas vacias como `[]`.
- `servifyApi.getServiceRecurrence` normaliza respuestas vacias como `null`.
- `RequestDetail` vuelve a validar que los encuentros sean un arreglo antes de guardarlos en estado.
- El detalle puede abrir solicitudes sin encuentros y mostrar `Todavia no hay encuentros programados`.
- Se corrigio un `ReferenceError` real en `RequestDetail`: la tarjeta `Agenda del servicio` usaba el icono `Clock`, pero ese icono no estaba importado desde `lucide-react`. Vite compilaba igual, pero al entrar al detalle React rompia el render.
- Se agrego `ScreenErrorBoundary` alrededor de la pantalla activa para que una excepcion de render no vuelva a dejar el telefono en blanco; ahora muestra un mensaje recuperable.
- Se actualizo el cache del service worker a `servify-pwa-v3` y se cambio la estrategia para JS/CSS a `networkFirst`, evitando que el celular conserve assets viejos despues de reconstruir Docker.

El 2 de julio de 2026 se corrigio un bug del frontend que podia romper la pantalla al entrar al detalle de una solicitud recurrente.

**Causa probable**

El componente `RequestDetail` mostraba el estado de recurrencia usando `replaceAll`. Esa API puede fallar en algunos WebViews o navegadores moviles mas viejos aunque el build de Vite compile correctamente. Cuando eso ocurre, React lanza una excepcion en render y el contenedor visual de la app queda en blanco.

**Solucion aplicada**

- Se reemplazo `replaceAll` por `replace(/_/g, " ")`.
- Se agrego `formatBackendState` para que estados o frecuencias faltantes no rompan la pantalla.
- La tarjeta `Agenda del servicio` ahora renderiza de forma defensiva ante datos incompletos.

Archivo modificado:

```text
frontend/src/app/components/RequestDetail.tsx
```

## Guia funcional paso a paso

Esta guia describe como debe funcionar el flujo completo desde el punto de vista de la app.

### 1. Crear una solicitud inmediata

1. El solicitante entra a `Solicitar o crear` o a `Nueva solicitud`.
2. Completa descripcion, categoria, modalidad, localidad, disponibilidad y precio sugerido.
3. En `Tipo de solicitud`, deja seleccionada la opcion `Ahora`.
4. El frontend envia:

```json
{
  "tipoProgramacion": "INMEDIATA",
  "disponibilidadRequerida": {
    "diaSemana": "MONDAY",
    "horaDesde": "09:00:00",
    "horaHasta": "18:00:00"
  }
}
```

5. El backend crea la solicitud en estado `BUSCANDO_PRESTADOR`.
6. El motor de matching busca publicaciones compatibles por categoria, zona, modalidad y disponibilidad.

### 2. Crear una solicitud programada

1. El solicitante crea una solicitud.
2. En `Tipo de solicitud`, elige `Programada`.
3. Selecciona fecha futura, hora desde y hora hasta.
4. El frontend calcula el dia de semana de esa fecha y lo envia como disponibilidad requerida.
5. El backend guarda:

```text
tipo_programacion = programada
fecha_programada_inicio = fecha y hora de inicio elegidas
fecha_programada_fin = fecha y hora de fin elegidas
```

6. El matching sigue usando disponibilidad requerida para no romper el motor existente.
7. Cuando un prestador acepta y el solicitante confirma, el backend crea un `servicio_encuentro` confirmado para esa fecha.
8. En el detalle de la solicitud debe aparecer la tarjeta `Agenda del servicio` con ese encuentro.

### 3. Crear una solicitud recurrente

1. El solicitante crea una solicitud.
2. En `Tipo de solicitud`, elige `Recurrente`.
3. Define fecha del primer encuentro, frecuencia, horario y una fecha de fin opcional. El dia de semana se calcula automaticamente desde esa fecha.
4. El frontend envia:

```json
{
  "tipoProgramacion": "RECURRENTE",
  "frecuenciaRecurrencia": "SEMANAL",
  "fechaInicioRecurrencia": "2026-07-07",
  "fechaFinRecurrencia": "2026-09-30"
}
```

5. Si no se informa `fechaFinRecurrencia`, el backend guarda la recurrencia sin fecha final. Luego crea la solicitud y una fila en `servicio_recurrencia` con estado `BUSCANDO_PRESTADOR`.
6. El prestador ve la solicitud como recurrente antes de aceptarla.
7. Cuando el solicitante confirma al prestador, la recurrencia pasa a `ACTIVA`.
8. El backend crea el primer encuentro confirmado segun la fecha/dia/hora configurados.

### 4. Proponer un segundo encuentro

1. La solicitud debe estar asignada a un prestador.
2. Solicitante o prestador abre el detalle.
3. En `Agenda del servicio`, toca `Programar segunda visita`.
4. Recien entonces se abre un dialogo para completar fecha, hora desde, hora hasta y mensaje opcional.
5. Al tocar `Proponer visita`, el backend crea un `servicio_encuentro` en estado `PROPUESTO` y cierra el dialogo.
6. La contraparte recibe una notificacion.
7. La contraparte puede aceptar o rechazar:
   - aceptar cambia el encuentro a `CONFIRMADO`;
   - rechazar cambia el encuentro a `RECHAZADO`.

### 5. Cancelar un encuentro o una recurrencia

1. `Cancelar esta visita` abre una confirmacion que aclara que solo se cancela esa fecha y que el programa continua.
2. `Cancelar todo el programa` abre otra confirmacion y aclara que se cancelan recurrencia, visitas abiertas, asignacion y solicitud.
3. Ambos dialogos ofrecen `Contactar` para abrir el chat antes de confirmar.
4. La cancelacion conserva el historial y no borra registros.
5. Cancelar una visita nunca cambia el estado de la recurrencia, solicitud o asignacion.
6. Cancelar la recurrencia es una operacion transaccional e idempotente y notifica una sola vez a la contraparte.

### 6. Que deberia validar QA

- Entrar al detalle de una solicitud inmediata no debe romper la app.
- Entrar al detalle de una solicitud programada debe mostrar `Agenda del servicio`.
- Entrar al detalle de una solicitud recurrente debe mostrar frecuencia, rango `desde/hasta` o `sin fecha final`, estado y opcion de cancelacion.
- Si no hay encuentros, debe mostrarse `Todavia no hay encuentros programados`.
- `Programar segunda visita` debe abrir el dialogo sin mostrar el formulario permanentemente en el detalle.
- Programar una segunda visita debe crear un encuentro propuesto y refrescar la lista sin cambiar de pantalla.
- Aceptar/rechazar un encuentro debe cambiar su estado.
- Abrir una solicitud recibida con distribucion `ENVIADA` debe permitir aceptarla o rechazarla desde el detalle.
- Cancelar una visita debe conservar activa la recurrencia.
- Cancelar la recurrencia debe dejar canceladas la solicitud y asignacion y no borrar historial.

## Como probar

### Solicitud programada

1. Crear una solicitud desde el frontend.
2. Elegir `Programada`.
3. Seleccionar fecha, hora desde y hora hasta.
4. Confirmar que aparece en la lista con el texto `Programada`.
5. Desde una cuenta prestadora compatible, aceptar la solicitud.
6. Desde la cuenta solicitante, confirmar el prestador.
7. En el detalle debe aparecer un encuentro confirmado.

### Segundo encuentro

1. Abrir una solicitud ya asignada.
2. Ir a `Agenda del servicio`.
3. Tocar `Programar segunda visita` y comprobar que se abre el dialogo.
4. Completar fecha, hora desde y hora hasta y proponer la visita.
5. Entrar desde la otra cuenta.
6. Aceptar o rechazar la propuesta.
7. Confirmar que el estado del encuentro cambia y que se genera notificacion.

### Servicio recurrente

1. Crear una solicitud desde el frontend.
2. Elegir `Recurrente`.
3. Definir fecha del primer encuentro, frecuencia, horario y, si corresponde, fecha final.
4. Aceptar desde un prestador compatible.
5. Confirmar el prestador desde el solicitante.
6. Verificar que la recurrencia queda activa y aparece el primer encuentro.
7. Cancelar la recurrencia desde cualquiera de las partes.

## Verificacion ejecutada

```text
docker build --target build -t servify-backend-test .
docker run --rm servify-backend-test ./mvnw test
docker compose build frontend backend
docker compose up -d --no-deps backend
docker compose up -d --no-deps frontend
```

Resultado:

- Backend: 38 tests OK dentro de Docker.
- Frontend: build de Vite OK dentro de Docker (2032 modulos).
- Flyway aplico `V17` y `V18` sobre PostgreSQL; reconcilio tanto la cancelacion heredada como el cierre incorrecto del primer encuentro recurrente.
- La solicitud recurrente real quedo `ASIGNADA / ACTIVA / ACTIVA`, con el encuentro del 13/07 `COMPLETADO` y el del 20/07 `CONFIRMADO`.
- Verificacion de navegador sobre `localhost:5173`: contenido visible, sin overlay, sin errores de consola y sin desborde horizontal a 390 px.
- Advertencia no bloqueante: Vite informa un chunk mayor a 500 kB.

## Avance

- [x] Diagnostico inicial de modelo, matching y frontend.
- [x] Documento de sprint final creado.
- [x] Backend de programacion.
- [x] Backend de encuentros.
- [x] Backend de recurrencias.
- [x] Frontend de solicitud programada/recurrente.
- [x] Frontend de encuentros desde detalle.
- [x] Suite de tests.
- [ ] Actualizacion Jira cuando el conector este disponible.

## Pulido funcional y visual - 12 de julio de 2026

- La cancelacion de una recurrencia por cualquiera de sus participantes ahora cancela tambien la solicitud, la asignacion activa y los encuentros propuestos o confirmados, sin borrar el historial.
- Cuando cancela el prestador, el solicitante recibe una notificacion `RECURRENCIA_CANCELADA` asociada a la solicitud.
- Antes de cancelar una recurrencia se muestra una confirmacion visual que recomienda avisar primero a la contraparte y explica el alcance de la accion.
- La agenda del prestador conserva segundas visitas futuras `PROPUESTO` o `CONFIRMADO` aunque la visita principal haya sido finalizada; las solicitudes o asignaciones canceladas siguen excluidas.
- Si la agenda no puede consultar el backend muestra el error real y no lo confunde con el estado `Sin servicios agendados`.
- La eliminacion de publicaciones requiere confirmacion explicita y protege contra envios duplicados.
- El alta y la configuracion de cuenta comparten un selector de foto con galeria, camara web y captura nativa movil como respaldo.
- Las fotos se redimensionan antes de guardarse localmente para evitar exceder la cuota del navegador.
- Servis usa variaciones deterministicas de pose y movimiento, respeta `prefers-reduced-motion` y mantiene contraste propio en modo oscuro.
- Se corrigieron las superficies nuevas de agenda, estadisticas, globos y confirmaciones para conservar la paleta azul/turquesa en tema oscuro.

### Regresion minima recomendada

1. Programar y confirmar una segunda visita, finalizar la visita principal y comprobar que el encuentro futuro permanece en `Mis servicios > Agenda`.
2. Cancelar una recurrencia como prestador y validar recurrencia, solicitud, asignacion y notificacion al solicitante.
3. Probar `Galeria` y `Camara` durante el registro y desde `Configuracion > Cuenta` en Android, iOS y navegador de escritorio.
4. Revisar agenda, estados vacios y dialogos de confirmacion en tema claro, oscuro y configuracion `Sistema`.

## Correccion de consistencia y simplificacion del detalle - 13 de julio de 2026

Esta seccion complementa e incluye el pulido previo del 12 de julio: camara/galeria, confirmacion de publicaciones, tema oscuro, agenda del prestador y cancelacion notificada de recurrencias se mantienen vigentes.

### Diagnostico y reparacion del caso real

- En PostgreSQL se encontro la solicitud `Clases de guitarra` con solicitud `asignada`, recurrencia `cancelada`, asignacion `activa` y su unica visita `cancelado`.
- La causa era una cancelacion persistida por etapas sin una transaccion que abarcara toda la operacion.
- `V17__reconciliar_cancelaciones_recurrentes.sql` repara de forma idempotente los registros heredados sin pisar estados terminales validos.
- La cancelacion de recurrencia ahora es transaccional e idempotente: reconcilia visitas abiertas, solicitud y asignacion; un reintento no duplica la notificacion.
- La cancelacion de una sola visita tambien es transaccional, pero no modifica recurrencia, solicitud ni asignacion.
- Aplicada `V17`, el caso real quedo `cancelada / cancelada / cancelada`, con la visita cancelada conservada en el historial.

### Detalle de solicitud mas claro

- Las solicitudes recibidas pendientes muestran `Aceptar solicitud` y `Rechazar` al comienzo del detalle.
- La descripcion queda visible y los datos secundarios del pedido se despliegan bajo demanda.
- `Programar segunda visita` abre un dialogo accesible; el formulario ya no ocupa permanentemente la agenda.
- Las visitas proximas se separan del historial cancelado, rechazado o completado.
- `Cancelar esta visita` y `Cancelar todo el programa` tienen textos, confirmaciones y consecuencias distintas.
- Los dialogos de cancelacion ofrecen `Contactar`, que abre directamente el chat con la contraparte.
- Como defensa adicional, una recurrencia `CANCELADA` hace que el detalle se muestre cancelado aunque llegue un estado historico inconsistente de solicitud.

### Poses reales de Servis

- La causa visual era un unico PNG: las variantes anteriores solo movian o inclinaban la misma pose.
- Se incorporaron cinco assets transparentes distintos (`coach`, `wave`, `peek`, `lean`, `bounce`) derivados de la lamina de marca.
- Cada ayuda selecciona una pose por contexto; no se espejan ni deforman los assets y `prefers-reduced-motion` conserva las variantes sin movimiento.

## Ciclo correcto de recurrencias y conexión visual - 13 de julio de 2026

### Diagnóstico confirmado

- La solicitud recurrente real `clases de guitarra . quiero aprender...` estaba `finalizada`, con asignación `finalizada`, recurrencia `activa`, un único encuentro `confirmado` y dos confirmaciones globales.
- La causa era de modelado: `confirmacion_finalizacion` cerraba la asignación completa. En una recurrencia, esas confirmaciones deben corresponder a un encuentro concreto.
- La agenda descartaba la recurrencia porque interpretaba la asignación finalizada como el fin de todo el programa.

### Decisión de ciclo de vida

- Mientras la recurrencia esté `ACTIVA`, la solicitud permanece `ASIGNADA` y la interfaz la muestra como **En curso / Programa activo**; no queda pendiente.
- Cada fecha es un `servicio_encuentro` independiente y admite confirmación bilateral.
- Cuando ambas partes confirman un encuentro:
  - el encuentro pasa a `COMPLETADO`;
  - se genera de forma idempotente la próxima fecha según la frecuencia;
  - solicitud, asignación y recurrencia continúan activas.
- Si la fecha siguiente supera `fecha_fin`, la recurrencia, asignación y solicitud pasan a `FINALIZADA`.
- Cancelar una visita cancela solo ese encuentro y materializa el siguiente si corresponde.
- Cancelar todo el programa mantiene el comportamiento transaccional: cancela la recurrencia, los encuentros abiertos, la asignación y la solicitud, y notifica a la contraparte.

### Persistencia y compatibilidad

- `V18__confirmaciones_por_encuentro_recurrente.sql` agrega:
  - `servicio_encuentro.recurrencia_servicio_id`;
  - `confirmacion_finalizacion.encuentro_servicio_id`;
  - unicidad por `(encuentro_servicio_id, rol_confirmante)`;
  - unicidad legacy por asignación solo para confirmaciones sin encuentro.
- El endpoint de confirmación acepta `encuentroId` opcional. El backend lo infiere para clientes anteriores y el frontend nuevo lo envía de forma explícita.
- `estado-asignacion` expone `encuentroActivoId` y las confirmaciones de la fecha actual sin marcar como finalizado todo el programa.
- La migración reconcilia el caso real: completa el primer encuentro ya confirmado, crea la próxima fecha y reabre solicitud/asignación cuando la recurrencia sigue activa.
- Las confirmaciones y cancelaciones usan bloqueos pesimistas con orden fijo `recurrencia → encuentro`; así, dos acciones simultáneas no dejan estados parciales y evitan interbloqueos.

### Ajustes de interfaz

- Las solicitudes recurrentes no muestran `Programar segunda visita`: sus fechas provienen de la regla de recurrencia.
- El detalle muestra `Confirmar encuentro`, la fecha actual y aclara que el programa continúa después de ambas confirmaciones.
- Las próximas ocurrencias se identifican como `Próxima sesión` y aparecen en la agenda del prestador.
- Al confirmar un prestador se muestra una animación breve de conexión entre solicitante, Servis y prestador; solo se dispara después de una respuesta exitosa del backend.
- `Contactar antes de cancelar` abre directamente el chat.
- Servis y su globo ahora reservan columnas separadas: la mascota no queda detrás del texto y la cola apunta hacia su posición en todas las poses, también en móvil, modo oscuro y movimiento reducido.

### Regresión agregada

- Confirmar ambos roles completa solo el encuentro y crea el siguiente.
- La misma persona puede confirmar otra fecha porque la unicidad es por encuentro.
- Completar la última fecha finaliza naturalmente el programa y sus entidades padre.
- Cancelar una visita conserva activa la serie y crea la próxima fecha.
- Cancelar la serie sigue cancelando las entidades padre y notificando una sola vez.
- Confirmar, cancelar o resolver acciones simultáneas queda serializado por bloqueos de base de datos.

## Pagos con Mercado Pago Checkout Pro - 13 de julio de 2026

### Diagnóstico y decisión

- El cierre anterior confiaba solo en la confirmación declarada por las partes: no existía una operación de pago ni una prueba de acreditación.
- Para la primera versión se eligió **Checkout Pro para Argentina**: Mercado Pago aloja el checkout y Servify valida el resultado desde el backend.
- La moneda es `ARS`. Un servicio no recurrente tiene un pago por asignación; una recurrencia tiene un pago independiente por cada encuentro confirmado.
- El dinero ingresa en esta versión a la cuenta de Mercado Pago configurada por Servify. Todavía no hay marketplace, split automático ni transferencia al prestador.

### Flujo implementado

1. Si la asignación todavía está `A convenir`, el detalle muestra `Acordar precio`: el solicitante puede coordinar por chat y cargar el importe en un formulario breve. El prestador ve el estado pendiente y un acceso directo al chat.
2. El precio se fija en la asignación; una contraoferta aceptada también actualiza el importe que se usará al confirmar al prestador.
3. El solicitante ve `Pagar y confirmar` únicamente cuando existe un monto mayor a cero.
4. El backend crea o reutiliza una preferencia asociada a una `external_reference` propia y abre Checkout Pro en otra pestaña.
5. Servify nunca confía en los parámetros del navegador: consulta a Mercado Pago y compara referencia, monto y moneda.
6. Con estado `APROBADO`, el backend registra de forma idempotente la confirmación del solicitante.
7. Recién entonces el prestador puede usar `Confirmar cobro y finalización`.
8. En una recurrencia, ambas confirmaciones completan solo ese encuentro y generan la próxima fecha; no cierran el programa completo.
9. Rechazos y cancelaciones permiten reabrir el checkout existente. Reembolsos o contracargos posteriores quitan la habilitación de confirmación del prestador.

### Backend y persistencia

- `V19__pagos_mercadopago_checkout_pro.sql` agrega `pago_servicio`, con monto, moneda, estado, preferencia, `payment_id`, `external_reference` y marcas de auditoría.
- Hay unicidad por asignación no recurrente, por encuentro recurrente, por preferencia, referencia externa y pago de Mercado Pago.
- La creación de checkout se serializa con bloqueo de asignación; la sincronización bloquea el pago. Esto evita preferencias duplicadas y confirmaciones repetidas.
- Los endpoints financieros derivan la identidad del token Bearer y no aceptan que un ID enviado por el cliente suplante a otro usuario.
- El endpoint para acordar precio valida el token Bearer, la propiedad de la solicitud, la relación con la asignación y que esta siga activa. Un precio ya fijado no puede reemplazarse desde este flujo.
- El webhook valida `x-signature` con HMAC SHA-256 y luego vuelve a consultar el pago a Mercado Pago.
- La obligación de pago es independiente de que el token esté disponible. Con `SERVIFY_MERCADOPAGO_REQUIRED=true`, una credencial faltante bloquea el cierre en vez de desactivar silenciosamente la regla.
- El endpoint anterior de finalización también exige un pago aprobado para solicitante y prestador; no existe un atajo desde el frontend.

### Contrato HTTP

```text
POST /api/v1/solicitudes/{solicitudId}/pagos/checkout
POST /api/v1/pagos/{pagoId}/sincronizacion
GET  /api/v1/solicitudes/{solicitudId}/pagos/estado?asignacionServicioId=...&encuentroId=...
POST /api/v1/pagos/mercadopago/webhook
```

- `checkout` y `sincronizacion` requieren una sesión activa del solicitante.
- `estado` solo responde a solicitante o prestador de la asignación.
- `webhook` no usa sesión de Servify: requiere la firma secreta de Mercado Pago.

### Configuración Docker

```text
SERVIFY_MERCADOPAGO_ACCESS_TOKEN=TEST-...
SERVIFY_MERCADOPAGO_RETURN_URL=https://dominio-publico.example/
SERVIFY_MERCADOPAGO_NOTIFICATION_URL=https://api-publica.example/api/v1/pagos/mercadopago/webhook
SERVIFY_MERCADOPAGO_WEBHOOK_SECRET=...
SERVIFY_MERCADOPAGO_REQUIRED=true
```

- Las variables ya están declaradas en `.env.example`, `docker-compose.yml` y `application.properties`; los secretos reales no se versionan.
- Mercado Pago no admite `localhost` como URL de retorno. En desarrollo local se omiten `back_urls` y el solicitante usa `Ya pagué, verificar pago`, que busca la operación por `external_reference`.
- En un entorno HTTPS público se habilitan retorno automático y webhook firmado.

### Interfaz

- La tarjeta de pago muestra monto ARS, estado real, enlace alternativo si el popup fue bloqueado y verificación manual.
- En solicitudes recurrentes, el formulario aclara `Precio sugerido por encuentro`: el monto acordado se cobra en cada sesión, no una vez por todo el programa.
- El retorno de Mercado Pago se sincroniza, limpia sus parámetros de la URL y abre la solicitud relacionada.
- El sondeo está acotado y se cancela al desmontar el componente.
- El prestador nunca ve la acción de cierre antes de la acreditación.
- Se agregaron estados accesibles, tema oscuro y respeto por `prefers-reduced-motion`.

### Cobertura agregada y verificación pendiente

- Se agregaron pruebas de bloqueo para ambos roles, aprobación idempotente, referencia/monto/ARS incorrectos, pago por encuentro, reintento tras rechazo, reembolso, firma del webhook y URLs públicas.
- La revisión estática quedó realizada. El build y la suite nuevos deben ejecutarse exclusivamente en Docker; en esta sesión quedaron pendientes porque el entorno de Codex agotó temporalmente su cuota de aprobación para acceder al daemon.
- Tampoco se ejecutó un cobro externo porque no había credenciales de prueba de Mercado Pago en `.env`.

### Alcance pendiente antes de producción

- Implementar Mercado Pago Marketplace/OAuth si el dinero debe ir directamente al prestador y Servify debe retener una comisión.
- Incorporar conciliación administrativa, comprobantes, devolución desde Servify y tratamiento operativo de disputas.
- Cancelar hoy una visita, una serie o una solicitud con pago aprobado no ejecuta una devolución. Hasta implementar refunds, debe gestionarse la devolución antes de cancelar; este caso no está listo para producción.
- Un reembolso posterior actualiza la verdad financiera, pero no deshace automáticamente un servicio que ambas partes ya cerraron; requiere conciliación.
- Configurar credenciales de prueba, dominio HTTPS y secreto de webhook antes de la demostración integrada.

### Demostración

- El guion completo y cronometrado quedó en `docs/demo-oral-servify.md`.
- La demostración se divide en cinco grabaciones reales y breves, preparadas para presentarlas en orden o editarlas manualmente.

## Correccion de cierre anticipado y cancelacion programada - 13 de julio de 2026

### Regla definitiva para solicitudes recurrentes

- El precio acordado se cobra por encuentro y cada pago se identifica por `asignacionServicioId + encuentroId`.
- El retorno guardado de Mercado Pago incluye el `encuentroId`; nunca se aplica el resultado de una visita a otra fecha de la misma solicitud.
- El checkout y la confirmacion bilateral de una visita recurrente se habilitan cuando finaliza su horario programado. Esto impide completar automaticamente fechas futuras.
- Ambas confirmaciones completan solo el encuentro actual. Si existe otra fecha, se crea o conserva en `CONFIRMADO` y los estados padre permanecen `ASIGNADA / ACTIVA / ACTIVA`.
- La solicitud, asignacion y recurrencia se finalizan juntas solo cuando ya no existe una fecha siguiente dentro del programa.

### Cancelacion desde agenda

- La cita que coincide con la fecha original de una solicitud `PROGRAMADA` representa el servicio principal. Al cancelarla se cancelan tambien las visitas pendientes, la asignacion y la solicitud dentro de la misma transaccion.
- Una segunda visita tiene otra fecha: `Cancelar esta visita` cambia solo ese encuentro y mantiene el servicio activo.
- El detalle y la agenda muestran textos distintos para evitar confundir ambos alcances y actualizan el estado inmediatamente despues de confirmar.

### Reparacion de datos existentes

- `V20__reconciliar_encuentros_futuros_y_programadas.sql` reabre encuentros recurrentes completados antes de su fecha y sus entidades padre cuando corresponde.
- Se conserva el pago aprobado y la confirmacion del solicitante; se elimina solamente la confirmacion prematura del prestador para que confirme la realizacion al finalizar la fecha real.
- La misma migracion cancela solicitudes programadas heredadas cuya cita principal ya estaba cancelada pero cuyos estados padre seguian activos.

### Regresion requerida

1. Completar una visita recurrente vencida y comprobar que solo esa fecha pasa a `COMPLETADO`.
2. Verificar que la proxima fecha aparece en ambas agendas, no permite pago anticipado y mantiene la serie activa.
3. Completar la ultima fecha y comprobar que recien entonces se finalizan solicitud, asignacion y recurrencia.
4. Cancelar la cita principal programada y comprobar estados `CANCELADA / CANCELADA`.
5. Cancelar una segunda visita y comprobar que solicitud y asignacion permanecen activas.

### Verificacion ejecutada

- Suite completa: 55 tests backend aprobados dentro de un contenedor Maven; tras el ultimo ajuste se repitieron los 8 tests de cancelacion con resultado correcto.
- `docker compose build backend frontend`: compilacion Java y build Vite aprobados; el unico aviso no bloqueante es el chunk principal mayor a 500 kB.
- Flyway aplico `V20` en PostgreSQL Docker sin errores.
- Caso recurrente real: solicitud `asignada`, asignacion `activa`, recurrencia `activa`; primera visita `completado` y futuras `confirmado` sin confirmacion prematura del prestador.
- Caso programado real: solicitud y asignacion quedaron `cancelada / cancelada`, con las visitas canceladas conservadas en el historial.
- Navegador en `localhost:5173`: contenido visible, sin overlay y sin errores de consola.
