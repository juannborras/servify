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

## Proximo paso critico de producto

- Monetizacion dentro de la app: implementar pagos in-app para poder cobrar una comision por los servicios contratados desde Servify.
- Definir pasarela de pago, estados de pago, comprobantes, reglas de comision y trazabilidad administrativa.

## Pendiente Jira

- Registrar o actualizar estas tareas en Jira cuando el acceso de Atlassian Rovo quede habilitado.
- Estado actual del acceso desde Codex: Atlassian Rovo devuelve 403 indicando que la app no esta instalada/autorizada en la instancia.
