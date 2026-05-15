# Historias de Usuario Backend — Sistema de Recaudo Diario

## HU-BE-001 — Gestión de roles y usuarios

**Como** administrador,  
**quiero** crear y administrar usuarios con roles,  
**para** controlar quién puede ingresar al sistema y qué acciones puede realizar.

### Criterios de aceptación

- El sistema debe permitir crear usuarios.
- Cada usuario debe tener un rol.
- Los roles iniciales serán `ADMINISTRADOR` y `TRABAJADOR`.
- El usuario debe autenticarse con credenciales.
- El sistema debe manejar usuarios activos e inactivos.
- Todas las operaciones deben guardar auditoría: `created_at`, `updated_at`, `deleted_at`, `updated_by`.

---

## HU-BE-002 — Autenticación con JWT

**Como** usuario del sistema,  
**quiero** iniciar sesión con usuario y contraseña,  
**para** acceder a las funcionalidades según mi rol.

### Criterios de aceptación

- El sistema debe validar usuario y contraseña.
- Si las credenciales son correctas, debe retornar un token JWT.
- Si las credenciales son incorrectas, debe retornar error controlado.
- El token debe incluir el identificador del usuario y su rol.
- Los endpoints protegidos deben requerir token válido.

---

## HU-BE-003 — Gestión de trabajadores

**Como** administrador,  
**quiero** registrar trabajadores cobradores,  
**para** asignarlos a rutas y controlar sus operaciones diarias.

### Criterios de aceptación

- El sistema debe permitir crear trabajadores.
- Un trabajador puede estar asociado a un usuario del sistema.
- Debe validar documento único cuando el trabajador esté activo.
- Debe permitir actualizar teléfono, dirección y estado.
- No debe eliminar físicamente trabajadores; debe usar `deleted_at`.
- Debe permitir consultar trabajadores activos.

---

## HU-BE-004 — Gestión de rutas

**Como** administrador,  
**quiero** crear rutas de cobro,  
**para** organizar los clientes por zona y recorrido.

### Criterios de aceptación

- El sistema debe permitir crear rutas.
- Una ruta debe tener nombre, zona, descripción y estado.
- Debe permitir listar rutas activas.
- Debe permitir actualizar una ruta.
- Debe permitir inactivar una ruta.
- No debe eliminar físicamente la ruta.

---

## HU-BE-005 — Asignación de trabajador a ruta

**Como** administrador,  
**quiero** asignar un trabajador a una ruta,  
**para** definir quién realizará el cobro diario.

### Criterios de aceptación

- Una ruta puede tener un trabajador activo asignado.
- Debe conservar historial de asignaciones anteriores.
- La asignación debe tener `fecha_inicio` y opcionalmente `fecha_fin`.
- Al asignar un nuevo trabajador, se debe cerrar la asignación anterior si aplica.
- Debe permitir consultar la ruta actual de un trabajador.

---

## HU-BE-006 — Gestión de clientes

**Como** trabajador o administrador,  
**quiero** registrar clientes,  
**para** asociarlos a una ruta y crear préstamos.

### Criterios de aceptación

- El sistema debe permitir crear clientes con nombre, documento, teléfono, dirección, barrio y observación.
- El documento puede ser opcional, pero si existe debe permitir búsqueda.
- El cliente debe quedar en estado `ACTIVO` por defecto.
- El sistema debe permitir actualizar datos del cliente.
- El sistema debe permitir bloquear o retirar clientes.
- No debe eliminar físicamente clientes.

---

## HU-BE-007 — Insertar cliente en una ruta con orden específico

**Como** trabajador,  
**quiero** agregar un nuevo cliente después de otro cliente existente en mi ruta,  
**para** mantener el orden real del recorrido.

### Criterios de aceptación

- El sistema debe recibir `ruta_id`, `cliente_id` y `orden_base`.
- Si el cliente se inserta después del orden 5, debe quedar con orden 6.
- Los clientes posteriores deben aumentar su orden automáticamente.
- El sistema no debe duplicar órdenes dentro de la misma ruta.
- El sistema debe validar que el cliente no esté ya activo en la misma ruta.
- El cambio debe quedar registrado en auditoría.

### Ejemplo

Antes:

```text
1. Cliente A
2. Cliente B
3. Cliente C
4. Cliente D
5. Cliente E
6. Cliente F
```

Después de insertar nuevo cliente después del 5:

```text
1. Cliente A
2. Cliente B
3. Cliente C
4. Cliente D
5. Cliente E
6. Cliente Nuevo
7. Cliente F
```

---

## HU-BE-008 — Configuración de límites por trabajador

**Como** administrador,  
**quiero** configurar límites de préstamo por trabajador,  
**para** controlar el monto, tasa y plazo que cada cobrador puede manejar.

### Criterios de aceptación

- El sistema debe permitir definir monto máximo por préstamo.
- El sistema debe permitir definir tasa mínima.
- El sistema debe permitir definir tasa máxima.
- El sistema debe permitir definir plazo máximo en días.
- Debe permitir activar o inactivar límites.
- Si el trabajador no tiene límites activos, no debe poder crear préstamos.

---

## HU-BE-009 — Crear préstamo desde la ruta

**Como** trabajador,  
**quiero** crear un préstamo directamente desde mi ruta,  
**para** entregar dinero al cliente y comenzar el cobro diario.

### Criterios de aceptación

- El trabajador debe poder ingresar monto prestado, tasa y plazo.
- El sistema debe calcular valor de interés.
- El sistema debe calcular total a pagar.
- El sistema debe calcular cuota diaria.
- El préstamo debe quedar en estado `ACTIVO`.
- El sistema debe validar los límites configurados del trabajador.
- Si el monto, tasa o plazo supera el límite permitido, debe bloquear la creación.
- No se requiere aprobación del administrador.
- El préstamo debe quedar asociado al cliente, ruta y trabajador.

---

## HU-BE-010 — Generación automática de cuotas del préstamo

**Como** sistema,  
**quiero** generar las cuotas diarias al crear un préstamo,  
**para** controlar los pagos esperados cada día.

### Criterios de aceptación

- Al crear un préstamo, el sistema debe generar una cuota por cada día del plazo.
- Cada cuota debe tener número, fecha, valor, saldo y estado.
- El estado inicial de cada cuota debe ser `PENDIENTE`.
- La suma de cuotas debe coincidir con el total a pagar.
- La última cuota debe ajustar diferencias por redondeo si existen.

---

## HU-BE-011 — Generar recaudo diario por ruta

**Como** sistema,  
**quiero** generar la planilla diaria de recaudo de una ruta,  
**para** que el trabajador vea sus clientes en el orden correcto.

### Criterios de aceptación

- El sistema debe generar un `recaudo_diario` por ruta, trabajador y fecha.
- Debe traer los clientes activos de la ruta.
- Debe respetar el orden definido en `ruta_clientes`.
- Debe asociar el préstamo activo de cada cliente, si existe.
- Debe calcular el valor esperado del día.
- El estado inicial debe ser `ABIERTO`.
- No debe generar dos recaudos abiertos para la misma ruta, trabajador y fecha.

---

## HU-BE-012 — Registrar pago de cliente

**Como** trabajador,  
**quiero** registrar el pago de un cliente,  
**para** actualizar el saldo del préstamo y el recaudo del día.

### Criterios de aceptación

- El sistema debe permitir registrar pago total o parcial.
- El pago debe estar asociado a cliente, préstamo, ruta, trabajador y recaudo.
- El pago debe actualizar la cuota correspondiente.
- El pago debe actualizar el saldo del préstamo.
- El pago debe actualizar el detalle del recaudo diario.
- El pago debe generar movimiento de caja tipo `PAGO_RECIBIDO`.
- No debe permitir pagos sobre préstamos `ANULADOS` o `PAGADOS`.
- Si el saldo llega a cero, el préstamo debe quedar en estado `PAGADO`.

---

## HU-BE-013 — Marcar cliente como no pagó

**Como** trabajador,  
**quiero** marcar que un cliente no pagó,  
**para** dejar registro de la visita del día.

### Criterios de aceptación

- El trabajador debe poder marcar un cliente como `NO_PAGO`.
- Debe permitir registrar una observación.
- No debe modificar el saldo del préstamo.
- No debe generar movimiento de caja.
- Debe actualizar el estado del detalle del recaudo diario.
- El cliente debe permanecer en la misma posición de la ruta.

---

## HU-BE-014 — Crear cliente y préstamo en una sola operación

**Como** trabajador,  
**quiero** registrar un cliente nuevo y crearle un préstamo en el mismo flujo,  
**para** agilizar la operación en campo.

### Criterios de aceptación

- El sistema debe crear el cliente.
- Debe insertarlo en la ruta después del cliente seleccionado.
- Debe crear el préstamo activo.
- Debe generar las cuotas.
- Debe actualizar la planilla diaria si ya existe.
- Debe generar movimiento de caja tipo `PRESTAMO_ENTREGADO`.
- Toda la operación debe ser transaccional.
- Si falla una parte, no debe guardar nada parcialmente.

---

## HU-BE-015 — Control de caja diaria del trabajador

**Como** administrador,  
**quiero** controlar la caja diaria de cada trabajador,  
**para** saber cuánto dinero recibió, prestó, recaudó y debe entregar.

### Criterios de aceptación

- El sistema debe permitir abrir caja diaria.
- La caja debe tener valor inicial.
- Cada préstamo creado debe sumar en `valor_prestamos_entregados`.
- Cada pago recibido debe sumar en `valor_recaudado`.
- El sistema debe calcular:

```text
valor_esperado_cierre = valor_inicial - valor_prestamos_entregados + valor_recaudado
```

- Debe permitir registrar valor entregado.
- Debe calcular diferencia.
- Debe permitir cerrar caja.
- No debe permitir cerrar dos veces la misma caja.

---

## HU-BE-016 — Movimientos de caja

**Como** sistema,  
**quiero** registrar cada movimiento de caja,  
**para** tener trazabilidad del dinero del trabajador.

### Criterios de aceptación

- Al abrir caja debe generarse movimiento `CAJA_INICIAL`.
- Al crear préstamo debe generarse movimiento `PRESTAMO_ENTREGADO`.
- Al registrar pago debe generarse movimiento `PAGO_RECIBIDO`.
- Al cerrar caja debe generarse movimiento `CIERRE_CAJA`.
- Cada movimiento debe tener tipo, valor, referencia y fecha.
- No deben eliminarse movimientos de caja.

---

## HU-BE-017 — Consulta de ruta diaria del trabajador

**Como** trabajador,  
**quiero** consultar mi ruta diaria,  
**para** ver los clientes que debo visitar en el orden correcto.

### Criterios de aceptación

- El endpoint debe retornar la ruta del trabajador autenticado.
- Debe retornar los clientes en orden ascendente.
- Debe mostrar estado del recaudo por cliente.
- Debe mostrar cuota esperada, valor pagado y saldo.
- Los clientes pagados no deben moverse de posición.
- Debe permitir filtrar por fecha.

---

## HU-BE-018 — Dashboard administrativo

**Como** administrador,  
**quiero** consultar indicadores generales del negocio,  
**para** tomar decisiones sobre rutas, cartera y recaudo.

### Criterios de aceptación

- Debe mostrar total prestado.
- Debe mostrar total recaudado del día.
- Debe mostrar cartera activa.
- Debe mostrar clientes en mora.
- Debe mostrar rutas activas.
- Debe mostrar trabajadores activos.
- Debe mostrar recaudo esperado vs recaudo real.

---

## HU-BE-019 — Reporte de clientes en mora

**Como** administrador,  
**quiero** consultar clientes en mora,  
**para** hacer seguimiento a la cartera vencida.

### Criterios de aceptación

- Debe listar clientes con cuotas vencidas.
- Debe mostrar ruta, trabajador, días de mora y saldo.
- Debe permitir filtrar por ruta.
- Debe permitir filtrar por trabajador.
- Debe permitir filtrar por rango de días de mora.

---

## HU-BE-020 — Auditoría general

**Como** administrador,  
**quiero** consultar la auditoría del sistema,  
**para** revisar acciones sensibles realizadas por usuarios.

### Criterios de aceptación

- Debe registrar creación de clientes.
- Debe registrar creación de préstamos.
- Debe registrar pagos.
- Debe registrar anulaciones.
- Debe registrar cambios de orden en ruta.
- Debe registrar cierres de caja.
- Debe guardar usuario, acción, tabla, registro, valor anterior y valor nuevo.
