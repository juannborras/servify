# Demo oral integral de Servify

Fecha: 13 de julio de 2026  
Objetivo: demostrar el producto completo de forma comercial, clara y reproducible, sin confundir una simulación visual con evidencia funcional.

## Resultado recomendado

Grabar **cinco capítulos cortos** para presentarlos en orden o ensamblarlos manualmente en **un solo video final de aproximadamente 11 minutos**. La exposición se narra en vivo; los clips quedan sin audio para evitar notificaciones, datos personales y repeticiones por cambios menores del discurso. Todos los clics y estados que aparecen deben provenir de capturas reales.

## Mensaje comercial de la presentación

Problema:

> Contratar un servicio informalmente obliga a buscar por un lado, coordinar por otro, pagar sin trazabilidad y confiar sin evidencia.

Propuesta:

> Servify reúne descubrimiento, matching, negociación, chat, pago, agenda, recurrencias y reputación en un único flujo verificable para Argentina.

Cierre:

> No mostramos pantallas aisladas: mostramos cómo una necesidad se convierte en un servicio coordinado, pagado, finalizado y calificado.

## Estado inicial obligatorio

No guardar contraseñas ni tokens en este documento o dentro de los videos. Preparar una tarjeta privada con cuatro accesos:

| Identidad | Rol | Estado necesario |
|---|---|---|
| `Solicitante demo` | Solicitante o ambos | Perfil completo, foto, Palermo/CABA |
| `Prestador demo` | Prestador o ambos | Perfil completo, foto, publicación activa compatible |
| `Admin demo` | Administrador | Cuenta activa y panel habilitado |
| `Comprador MP demo` | Cuenta de prueba de Mercado Pago Argentina | Usuario comprador de prueba, sin dinero real |

Abrir antes de grabar:

- Navegador/perfil A: Servify como solicitante.
- Navegador/perfil B: Servify como prestador.
- Navegador/perfil C: Servify como administrador.
- Ventana de incógnito: checkout de Mercado Pago con comprador de prueba.

Mantener los tres navegadores en el mismo tamaño y con zoom al 100 %. Desactivar autocompletado visible, extensiones, favoritos, emails emergentes y notificaciones del sistema.

## Datos funcionales de preparación

Crear los datos desde la app, no editando PostgreSQL manualmente:

1. Una categoría estable para la demo, por ejemplo `Clases particulares`.
2. Una publicación activa del prestador:
   - título claro;
   - modalidad virtual o presencial;
   - zona compatible con el solicitante;
   - disponibilidad compatible;
   - precio fácil de leer, por ejemplo `$20.000`.
3. Una calificación histórica con comentario para que el perfil público tenga prueba social.
4. Una solicitud programada ya asignada, reservada para mostrar segunda visita.
5. Una solicitud recurrente semanal activa con encuentro actual y próxima sesión.
6. Una segunda recurrencia descartable, reservada para cancelar todo el programa.
7. Una publicación descartable llamada `Publicación de moderación demo`.
8. Una categoría descartable llamada `Categoría demo temporal`.

La solicitud inmediata del flujo principal se crea durante la grabación. Cada nueva toma de pago requiere una solicitud nueva; un pago aprobado no debe reutilizarse ni forzarse a volver atrás.

## Preparación técnica, solo Docker

Desde la raíz del proyecto:

```powershell
docker compose up -d --build
docker compose ps
```

Antes de grabar verificar:

- PostgreSQL, backend y frontend saludables.
- `http://localhost:5173` abre sin errores.
- Los dos usuarios pueden iniciar sesión simultáneamente.
- El matching encuentra la publicación preparada.
- Las fechas programadas están en el futuro.
- La agenda muestra la próxima sesión recurrente.
- Las credenciales configuradas son **de prueba**, nunca productivas.
- El checkout se completa con una cuenta compradora de prueba en incógnito.
- El retorno vuelve a Servify y el backend muestra `Pago aprobado`.

Mercado Pago recomienda efectuar las compras de Checkout Pro en incógnito con una cuenta compradora de prueba para evitar mezcla de credenciales. Las credenciales privadas pertenecen al backend y no deben aparecer en pantalla ni quedar dentro del video.

No ejecutar `docker compose down -v` antes de la presentación: eliminaría los datos preparados. Si una toma falla, crear una solicitud nueva con sufijo `Toma 2`; es más seguro que alterar estados terminales.

## División eficiente de las grabaciones

| Archivo | Duración objetivo | Qué demuestra |
|---|---:|---|
| `01-producto-y-descubrimiento.mp4` | 1:30 | Acceso, alta, búsqueda, perfil público y publicación |
| `02-flujo-principal-y-pago.mp4` | 4:00 | Solicitud, matching, contraoferta, chat, asignación, Mercado Pago, cierre y calificación |
| `03-agenda-y-recurrencias.mp4` | 3:00 | Programación, segunda visita, recurrencia y dos niveles de cancelación |
| `04-confianza-y-preferencias.mp4` | 1:30 | Reputación, foto, historial, configuración, dark mode y PWA |
| `05-administracion.mp4` | 1:15 | Usuarios, publicaciones, categorías y notificaciones administrativas |

Duración total aproximada: 11:15. Si un capítulo dura menos, conservar el corte natural o ajustarlo durante la edición manual.

## Cobertura funcional de la demo

| Área de Servify | Capítulo |
|---|---:|
| Login, registro, roles y foto por cámara/galería | 1 |
| Categorías, búsqueda de prestadores y perfil público | 1 |
| Crear, editar, pausar, reactivar y eliminar publicaciones | 1 |
| Solicitud inmediata, matching, aceptar/rechazar y contraofertar | 2 |
| Chat y notificaciones del servicio | 2 |
| Confirmación de prestador y animación de conexión | 2 |
| Mercado Pago, validación backend y finalización bilateral | 2 |
| Calificación con comentario y reputación | 2 y 4 |
| Solicitud programada y segunda visita | 3 |
| Recurrencia, agenda y confirmación por encuentro | 3 |
| Cancelar una fecha o cancelar toda la serie | 3 |
| Historial, filtros y repetir solicitud | 4 |
| Cuenta, privacidad, alertas, tema y PWA | 4 |
| Usuarios, publicaciones y categorías administrativas | 5 |
| Recuperación de contraseña y OAuth social | Clip de respaldo solo si SMTP/OIDC están configurados |

En las pantallas de una solicitud recibida conviene detener el cursor un segundo sobre `Aceptar` y `Rechazar`, y luego ejecutar la contraoferta. Así se demuestra que existen las tres decisiones sin destruir tres solicitudes distintas durante el capítulo principal.

## Capítulo 1 — Producto y descubrimiento

### Secuencia de pantalla

| Tiempo | Rol | Acción visible | Narración sugerida |
|---:|---|---|---|
| 0:00–0:12 | Sin sesión | Mostrar splash, login y acceso al registro | “Servify nace mobile-first y permite ingresar o crear una cuenta según cómo se usará la plataforma.” |
| 0:12–0:25 | Registro | Mostrar roles y selector `Galería / Cámara`; no crear una cuarta cuenta | “El alta adapta la experiencia al solicitante, al prestador o a ambos, e incluye foto desde cámara o galería.” |
| 0:25–0:45 | Solicitante | Entrar a Explorar, abrir categorías populares y luego todas | “El descubrimiento empieza por necesidad y categoría, sin sobrecargar la pantalla.” |
| 0:45–1:00 | Solicitante | Buscar al prestador por usuario y abrir su perfil | “También se puede buscar directamente a una persona y revisar reputación antes de contratar.” |
| 1:00–1:15 | Solicitante | Desplegar servicios activos y comentarios | “La confianza proviene de publicaciones reales y calificaciones vinculadas a trabajos terminados.” |
| 1:15–1:30 | Prestador | Mostrar `Mis servicios > Publicaciones`, pausar/reactivar, editar y abrir la confirmación de eliminación sin ejecutarla | “El prestador define qué ofrece, precio, cobertura y horarios, y administra cada publicación sin perder el historial.” |

### Evidencia mínima que debe quedar visible

- Navegación inferior.
- Una pose contextual de Servis sin tapar texto.
- Búsqueda con resultado real.
- Perfil público con servicio y comentario.
- Publicación activa del prestador.
- Acciones de pausar, editar y eliminar con confirmación.

## Capítulo 2 — Flujo principal y pago

Este es el capítulo central. Conviene ensayarlo dos veces sin grabar porque cambia entre dos sesiones y un checkout externo.

### Secuencia de pantalla

| Tiempo | Rol | Acción visible | Narración sugerida |
|---:|---|---|---|
| 0:00–0:25 | Solicitante | Crear solicitud inmediata con categoría, descripción, zona, disponibilidad y precio | “El solicitante describe la necesidad una sola vez; Servify conserva los datos necesarios para el matching.” |
| 0:25–0:40 | Solicitante | Mostrar estado `Buscando prestador` | “La solicitud entra al motor de compatibilidad por categoría, zona, modalidad y horario.” |
| 0:40–0:58 | Prestador | Abrir notificación o Solicitudes y entrar a `Ver detalle` | “El prestador recibe el pedido y puede responder desde el detalle, sin perder contexto.” |
| 0:58–1:18 | Prestador | Emitir contraoferta con precio y comentario | “Si las condiciones no cierran, negocia dentro de la plataforma y el motivo queda trazado.” |
| 1:18–1:35 | Solicitante | Ver la contraoferta y aceptar el precio | “El solicitante conserva el control: puede aceptar o seguir buscando.” |
| 1:35–1:52 | Ambos | Abrir chat, enviar un mensaje breve y volver al detalle | “La coordinación queda asociada a la solicitud y genera notificaciones internas.” |
| 1:52–2:12 | Solicitante | Confirmar al prestador y dejar visible la animación de conexión | “La asignación solo se consolida cuando el solicitante confirma; Servis refuerza visualmente esa conexión.” |
| 2:12–2:28 | Solicitante | Ir a finalización y mostrar `Pagar y confirmar` con monto acordado | “Para cerrar, el solicitante no confirma de palabra: paga el monto acordado.” |
| 2:28–3:00 | Comprador MP | Abrir Checkout Pro de prueba, completar una compra aprobada y volver a Servify | “Mercado Pago procesa el medio de pago; Servify no guarda datos de tarjeta.” |
| 3:00–3:18 | Solicitante | Tocar `Ya pagué, verificar pago` si el retorno no sincronizó solo; mostrar `Pago aprobado` | “El backend verifica la acreditación real antes de registrar la confirmación del solicitante.” |
| 3:18–3:35 | Prestador | Refrescar detalle; mostrar que la acción estaba bloqueada antes del pago y tocar `Confirmar cobro y finalización` | “El prestador solo puede cerrar cuando el pago fue aprobado; luego confirma cobro y trabajo terminado.” |
| 3:35–3:52 | Ambos | Mostrar solicitud finalizada y notificaciones | “Las dos partes ven el mismo estado y queda una trazabilidad completa.” |
| 3:52–4:00 | Solicitante | Abrir calificación, puntuar y escribir comentario | “La reputación nace de servicios efectivamente cerrados, no de opiniones anónimas.” |

### Reglas que deben explicarse con precisión

- `Pagar y confirmar` inicia Checkout Pro para el solicitante.
- Un retorno del navegador no se considera suficiente: el servidor consulta a Mercado Pago.
- El pago aprobado registra automáticamente la confirmación del solicitante.
- El prestador ve el estado, pero no puede confirmar finalización antes de la acreditación.
- El cierre completo sucede cuando el prestador confirma cobro y finalización.
- En una recurrencia, el pago y las confirmaciones corresponden al encuentro actual, no a toda la serie.
- El precio acordado de una recurrencia es **por encuentro**; conviene decirlo al crear el pedido y al mostrar el monto del checkout.
- En esta primera versión, la preferencia se crea con la cuenta de Mercado Pago configurada por Servify y el dinero ingresa a esa cuenta.
- Todavía no existe split ni transferencia automática al prestador. El botón del prestador confirma el pago visible y la finalización operativa; la liquidación al prestador queda fuera de esta integración inicial.

### Plan B honesto para Mercado Pago

Grabar el checkout aprobado antes de la exposición y conservar ese clip local. Si el servicio externo falla durante la venta:

1. reproducir la captura real previamente grabada;
2. volver a la app y mostrar el pago aprobado persistido;
3. decir que se usa el ambiente de prueba;
4. no presentar un mock o una edición estática como si fuera una transacción real.

Sin credenciales de prueba no se debe afirmar que el pago E2E fue demostrado.

## Capítulo 3 — Agenda, segunda visita y recurrencias

### Secuencia de pantalla

| Tiempo | Rol | Acción visible | Narración sugerida |
|---:|---|---|---|
| 0:00–0:25 | Solicitante | Abrir una solicitud programada asignada y mostrar fecha/hora | “Una solicitud puede ser inmediata, programada o recurrente; la elección se guarda como parte del acuerdo.” |
| 0:25–0:48 | Solicitante | Tocar `Programar segunda visita`, completar modal y proponer | “En un servicio puntual, una segunda visita aparece solo bajo demanda para no recargar el detalle.” |
| 0:48–1:05 | Prestador | Aceptar la visita propuesta | “La contraparte debe aceptar; recién entonces queda confirmada.” |
| 1:05–1:20 | Prestador | Abrir `Mis servicios > Agenda` y mostrar la visita | “La agenda deriva de encuentros confirmados, no de tarjetas decorativas.” |
| 1:20–1:42 | Solicitante | Abrir la recurrencia semanal activa; mostrar frecuencia, encuentro actual y próxima sesión | “En recurrencias no existe ‘segunda visita’: las fechas nacen automáticamente de la frecuencia.” |
| 1:42–2:02 | Ambos | Mostrar confirmación/pago por encuentro y próxima sesión generada | “Cada encuentro tiene pago y confirmación bilateral propios; completar uno mantiene activo el programa y crea el siguiente.” |
| 2:02–2:25 | Prestador | En recurrencia preparada, tocar `Cancelar esta visita`, abrir `Contactar` y luego cancelar | “Cancelar una fecha no cancela el acuerdo completo. El chat permite avisar antes y la serie continúa.” |
| 2:25–2:48 | Solicitante | Mostrar próxima sesión y programa todavía activo | “La agenda y el detalle reflejan inmediatamente la próxima ocurrencia.” |
| 2:48–3:00 | Prestador | En la recurrencia descartable, abrir `Cancelar todo el programa` y mostrar confirmación, sin demorar en el texto | “Cancelar la serie sí cancela visitas abiertas, asignación y solicitud, y notifica a la contraparte.” |

### Evidencia mínima

- Modal de segunda visita en solicitud no recurrente.
- Ausencia de `Programar segunda visita` en recurrencia.
- Próxima sesión visible en detalle y agenda.
- Diferencia explícita entre `Cancelar visita` y `Cancelar serie`.
- Botón `Contactar antes de cancelar` abriendo el chat.

## Capítulo 4 — Confianza y preferencias

### Secuencia de pantalla

| Tiempo | Rol | Acción visible | Narración sugerida |
|---:|---|---|---|
| 0:00–0:15 | Usuario | Mostrar perfil, reputación y comentarios | “La reputación combina puntaje y comentarios asociados a servicios reales.” |
| 0:15–0:30 | Usuario | Editar cuenta y abrir opciones de foto `Galería / Cámara` | “El perfil se mantiene desde la misma experiencia usada en el registro.” |
| 0:30–0:43 | Usuario | Cambiar claro → oscuro → sistema | “La apariencia persiste y conserva contraste en los componentes operativos.” |
| 0:43–0:55 | Usuario | Mostrar preferencias de notificación y privacidad | “Cada usuario decide qué avisos y datos visibles mantener.” |
| 0:55–1:12 | Solicitante | Abrir Solicitudes, cambiar filtros temporales y tocar `Repetir` para mostrar datos precargados | “El historial no se borra: se filtra, y un pedido anterior puede reutilizarse sin volver a escribir todo.” |
| 1:12–1:22 | Usuario | Abrir notificaciones, marcar una como leída y mostrar eliminación individual | “Los eventos importantes quedan persistidos y pueden administrarse desde la bandeja.” |
| 1:22–1:30 | Navegador | Mostrar opción de instalación PWA o la app ya instalada | “Servify puede instalarse desde el navegador sin perder su base web.” |

Recuperación por email y login social dependen de SMTP/OIDC externos. Si están configurados, agregarlos como clip de respaldo de 20 segundos; si no, mostrar únicamente la entrada de la interfaz y declarar la dependencia, sin simular un email o una autenticación exitosa.

## Capítulo 5 — Administración

### Secuencia de pantalla

| Tiempo | Rol | Acción visible | Narración sugerida |
|---:|---|---|---|
| 0:00–0:15 | Admin | Entrar desde Configuración al panel | “La administración aparece solo para una sesión autenticada con rol administrador.” |
| 0:15–0:32 | Admin | Buscar usuario y abrir detalle con publicaciones | “El equipo puede revisar contexto y reputación antes de actuar.” |
| 0:32–0:48 | Admin | Desactivar la publicación descartable y luego reactivarla | “La moderación es lógica y reversible; no borra historial.” |
| 0:48–1:03 | Admin | Crear `Categoría demo temporal`, desactivarla y reactivarla | “Las categorías se administran sin tocar la base manualmente.” |
| 1:03–1:15 | Usuario afectado | Mostrar notificación administrativa | “La acción no queda silenciosa: el usuario recibe un aviso persistido.” |

No suspender la cuenta solicitante, prestadora o administradora usada por la demo. Trabajar únicamente con datos descartables.

## Orden de grabación más seguro

El orden de archivos no debe ser el mismo que el orden de captura. Grabar en este orden reduce roturas de estado:

1. Capítulo 4, porque casi no muta datos.
2. Capítulo 1, sin guardar una publicación nueva.
3. Capítulo 5, reactivando todo antes de terminar.
4. Capítulo 3, usando dos recurrencias preparadas distintas.
5. Capítulo 2 al final, porque consume una solicitud y un pago nuevos.

Después renombrar los archivos con el orden narrativo `01` a `05`.

## Técnica de captura

- Resolución: 1920×1080.
- Frecuencia: 30 fps.
- Zoom del navegador: 100 %.
- Cursor visible, movimientos cortos.
- Esperar entre 1 y 2 segundos después de cada cambio de estado.
- Grabar sin micrófono; narrar en vivo.
- Pausar la captura al cambiar de perfil de navegador para evitar mostrar escritorios o credenciales.
- Ocultar barra de favoritos, otras pestañas, email, tokens, datos de tarjeta y DevTools.
- Usar nombres, direcciones y fotos ficticias autorizadas.

No acelerar tanto los formularios que parezca una simulación. Se pueden eliminar esperas y cambios de ventana, pero deben conservarse el clic que inicia la acción y el estado que devuelve el backend.

## Checklist final de venta

- [ ] Los cinco clips son capturas reales y no contienen secretos.
- [ ] El pago usa credenciales y comprador de prueba.
- [ ] Se ve `Pago aprobado` antes de habilitar al prestador.
- [ ] El prestador confirma cobro y finalización.
- [ ] Se ve una próxima sesión recurrente en la agenda.
- [ ] Cancelar una fecha conserva activa la serie.
- [ ] Cancelar la serie afecta todo el programa y genera notificación.
- [ ] El modo oscuro mantiene contraste.
- [ ] La moderación termina reactivando los datos descartables.
- [ ] El MP4 abre sin internet desde una copia local y otra de respaldo.
- [ ] Hay un plan B pregrabado para Checkout Pro.
- [ ] La explicación distingue funcionalidades actuales de integraciones externas no configuradas.

## Respuestas breves para preguntas probables

**¿Servify guarda la tarjeta?**  
No. Checkout Pro procesa los datos y el backend verifica el estado del pago con Mercado Pago.

**¿El prestador puede cerrar sin pago?**  
No. La confirmación de cobro y finalización se habilita únicamente después del pago aprobado.

**¿El dinero llega directamente al prestador?**  
No en esta etapa. Mercado Pago acredita en la cuenta configurada de Servify; todavía no se implementó split ni liquidación automática al prestador.

**¿Qué ocurre si se cancela una visita recurrente?**  
Se cancela esa fecha y el programa continúa. Cancelar el programa es una acción distinta y explícita.

**¿Por qué la recurrencia sigue en curso después del primer encuentro?**  
Porque el estado padre representa el programa; cada encuentro conserva pago y confirmaciones propios.

**¿Las notificaciones son push?**  
Actualmente son internas y persistidas. Push nativo es una evolución futura.

**¿El chat es tiempo real?**  
Es persistido y actualiza por consultas periódicas; WebSocket es una evolución posible.

**¿La app es nativa?**  
Es una PWA mobile-first instalable desde navegador; comparte una única base web mantenible.

## Referencias operativas

- [Mercado Pago — Realizar compras de prueba con Checkout Pro](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/integration-test/test-purchases)
- [Mercado Pago — Credenciales y cuenta compradora de prueba](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/integration-test)
