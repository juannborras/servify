# Sprint final - notas para informe de avances

Estas correcciones pertenecen al sprint final y deben considerarse al armar el informe de avances.

## Correcciones funcionales recientes

- Pantalla de solicitudes: mostrar el nombre del prestador en lugar de un ID tecnico.
- Confirmacion de prestador: el solicitante solo debe poder confirmar cuando un prestador ya acepto la solicitud.
- Calificaciones: el solicitante califica al prestador y el prestador califica al cliente, incluso cuando ambos usuarios tienen tipo de cuenta "ambos roles".
- Calificaciones ya enviadas: ocultar el boton de calificar y mostrar la calificacion emitida con el mensaje correspondiente.
- Flujo post-aceptacion: al aceptar una solicitud, navegar directamente al detalle de esa solicitud.
- Solicitudes en curso: ordenar las solicitudes activas/en curso arriba en la pantalla.
- Despliegue local: reconstruir el contenedor frontend cuando haya cambios para evitar servir bundles viejos en `localhost:5173`.
- Precio a convenir: corregido el guardado desde el detalle al ejecutar la lectura bloqueante de la asignacion dentro de una transaccion.
- Listado de solicitudes: cada tarjeta identifica si la solicitud es `Actual`, `Programada` o `Recurrente`.
- Detalle de pago: eliminado el texto tecnico y redundante sobre la coordinacion interna; solo se muestra una confirmacion breve cuando el encuentro o servicio ya fue confirmado.

## Monetizacion

- Implementada la primera versión con Mercado Pago Checkout Pro en ARS: `Pagar y confirmar`, verificación del servidor y cierre posterior del prestador.
- En servicios recurrentes el pago corresponde a cada encuentro, no a todo el programa.
- Siguiente paso crítico: Mercado Pago Marketplace/OAuth, reglas de comisión, split al prestador, comprobantes, devoluciones y conciliación administrativa.

## Pendiente Jira

- Registrar o actualizar estas tareas en Jira cuando el acceso de Atlassian Rovo quede habilitado.
- Estado actual del acceso desde Codex: Atlassian Rovo devuelve 403 indicando que la app no esta instalada/autorizada en la instancia.

## Consistencia de agenda y recurrencias - 13 de julio de 2026

- Cada pago recurrente queda asociado a una unica visita. El retorno de Mercado Pago tambien conserva el `encuentroId`, por lo que no puede reutilizarse al avanzar a la siguiente fecha.
- Un encuentro recurrente futuro no puede pagarse ni confirmarse como realizado antes de que termine su horario. Al completar una fecha, la siguiente queda `CONFIRMADA` y la solicitud, asignacion y recurrencia siguen activas.
- La serie solo pasa a `FINALIZADA` cuando se completa la ultima fecha definida por la recurrencia.
- Cancelar la cita principal de una solicitud `PROGRAMADA` desde la agenda ahora cancela tambien sus visitas pendientes, la asignacion y la solicitud. Cancelar una segunda visita conserva activo el servicio.
- La interfaz distingue claramente `Cancelar servicio programado` de `Cancelar esta visita` y refresca agenda y estado de la solicitud en una sola accion.
- La migracion `V20__reconciliar_encuentros_futuros_y_programadas.sql` repara los casos heredados sin borrar pagos aprobados ni historial.
- Verificacion final: suite completa de 55 tests y regresion puntual de 8 tests OK, builds Docker de backend/frontend OK, Flyway en `v20` y carga del frontend sin errores de consola ni overlay.
