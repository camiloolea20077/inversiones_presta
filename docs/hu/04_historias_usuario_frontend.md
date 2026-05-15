# Historias de Usuario Frontend — Sistema de Recaudo Diario

## HU-FE-001 — Login del sistema

**Como** usuario,  
**quiero** iniciar sesión,  
**para** acceder al sistema según mi rol.

### Criterios de aceptación

- Debe mostrar formulario de usuario y contraseña.
- Debe validar campos obligatorios.
- Debe consumir el endpoint de autenticación.
- Debe guardar el token JWT.
- Debe redirigir según rol:
  - Administrador al dashboard.
  - Trabajador a su ruta diaria.
- Debe mostrar mensaje si las credenciales son incorrectas.

---

## HU-FE-002 — Layout para administrador

**Como** administrador,  
**quiero** tener un menú principal,  
**para** navegar entre los módulos del sistema.

### Criterios de aceptación

- Debe mostrar menú lateral o superior.
- Debe incluir:
  - Dashboard.
  - Trabajadores.
  - Rutas.
  - Clientes.
  - Préstamos.
  - Pagos.
  - Caja diaria.
  - Mora.
  - Auditoría.
  - Configuración.
- Debe permitir cerrar sesión.
- Debe adaptarse a pantalla de escritorio y tablet.

---

## HU-FE-003 — Layout móvil para trabajador

**Como** trabajador,  
**quiero** tener una vista optimizada para celular,  
**para** cobrar en ruta de forma rápida.

### Criterios de aceptación

- La vista debe ser mobile first.
- Debe mostrar resumen de la ruta del día.
- Debe mostrar los clientes en tarjetas.
- Cada tarjeta debe mostrar:
  - Orden.
  - Nombre.
  - Dirección.
  - Cuota.
  - Saldo.
  - Estado.
  - Botones de acción.
- Debe mantener el orden de la ruta aunque se registren pagos.
- Debe ser fácil de usar con una mano.

---

## HU-FE-004 — Vista de ruta diaria del trabajador

**Como** trabajador,  
**quiero** ver mi ruta diaria,  
**para** saber a qué clientes debo cobrar.

### Criterios de aceptación

- Debe consultar la ruta diaria del trabajador autenticado.
- Debe mostrar la fecha actual.
- Debe mostrar total esperado.
- Debe mostrar total recaudado.
- Debe mostrar clientes pagados, pendientes, parciales y no pago.
- Debe mostrar las tarjetas en el orden recibido del backend.
- No debe ordenar por estado ni por hora de pago.

---

## HU-FE-005 — Tarjeta de cliente en ruta

**Como** trabajador,  
**quiero** ver la información resumida de cada cliente,  
**para** cobrar rápidamente.

### Criterios de aceptación

- La tarjeta debe mostrar:
  - Número de orden.
  - Nombre del cliente.
  - Teléfono.
  - Dirección.
  - Cuota diaria.
  - Saldo pendiente.
  - Estado del día.
- Debe tener botones:
  - Registrar pago.
  - Pago parcial.
  - No pagó.
  - Ver detalle.
  - Agregar cliente cerca.
- El color o etiqueta debe cambiar según estado:
  - Pendiente.
  - Pagado.
  - Parcial.
  - No pagó.
  - Sin préstamo.

---

## HU-FE-006 — Registrar pago

**Como** trabajador,  
**quiero** registrar el pago de un cliente,  
**para** actualizar el recaudo del día.

### Criterios de aceptación

- Debe abrir un modal de pago.
- Debe mostrar cliente, cuota esperada y saldo.
- Debe permitir ingresar valor recibido.
- Debe permitir seleccionar forma de pago.
- Debe permitir agregar observación.
- Debe validar que el valor sea mayor a cero.
- Al guardar, debe actualizar la tarjeta del cliente.
- El cliente debe conservar su posición en la ruta.

---

## HU-FE-007 — Registrar pago parcial

**Como** trabajador,  
**quiero** registrar un pago menor a la cuota diaria,  
**para** reflejar que el cliente abonó parcialmente.

### Criterios de aceptación

- Debe permitir ingresar un valor menor a la cuota esperada.
- Debe marcar la tarjeta como `PARCIAL`.
- Debe mostrar el valor pagado.
- Debe actualizar el total recaudado de la ruta.
- Debe conservar el orden del cliente.

---

## HU-FE-008 — Marcar cliente como no pagó

**Como** trabajador,  
**quiero** marcar que un cliente no pagó,  
**para** dejar registro de la visita.

### Criterios de aceptación

- Debe abrir modal de confirmación.
- Debe permitir ingresar observación.
- Debe marcar la tarjeta como `NO_PAGO`.
- No debe sumar valor al recaudo.
- El cliente debe quedar en el mismo orden.

---

## HU-FE-009 — Agregar cliente cerca

**Como** trabajador,  
**quiero** agregar un cliente nuevo después del cliente actual,  
**para** mantener el orden real de la ruta.

### Criterios de aceptación

- Desde una tarjeta debe existir botón `Agregar cliente cerca`.
- El formulario debe indicar:
  - Después de qué cliente se agregará.
  - Nuevo orden calculado.
- Debe permitir ingresar datos del cliente.
- Debe permitir continuar a datos del préstamo.
- Al guardar, el nuevo cliente debe aparecer inmediatamente después del cliente base.
- Los demás clientes deben desplazarse visualmente una posición.

---

## HU-FE-010 — Crear cliente y préstamo desde ruta

**Como** trabajador,  
**quiero** crear cliente y préstamo en el mismo formulario,  
**para** prestar dinero directamente en campo.

### Criterios de aceptación

- El formulario debe capturar datos del cliente:
  - Nombre.
  - Documento.
  - Teléfono.
  - Dirección.
  - Barrio.
  - Observación.
- El formulario debe capturar datos del préstamo:
  - Monto.
  - Tasa.
  - Plazo.
  - Tipo de interés.
- Debe mostrar simulación:
  - Valor prestado.
  - Interés.
  - Total a pagar.
  - Cuota diaria.
- Debe validar campos obligatorios.
- Debe enviar la solicitud al backend.
- Si el backend acepta, debe mostrar mensaje exitoso.
- Si el backend rechaza por límites, debe mostrar el motivo.

---

## HU-FE-011 — Resumen de caja del trabajador

**Como** trabajador,  
**quiero** ver mi caja diaria,  
**para** saber cuánto dinero tengo, cuánto presté y cuánto debo entregar.

### Criterios de aceptación

- Debe mostrar:
  - Caja inicial.
  - Préstamos entregados.
  - Recaudo recibido.
  - Valor esperado de cierre.
- Debe actualizarse después de crear préstamos.
- Debe actualizarse después de registrar pagos.
- Debe tener botón para cerrar recorrido o cierre de caja si aplica.

---

## HU-FE-012 — Dashboard administrativo

**Como** administrador,  
**quiero** ver un resumen general del sistema,  
**para** controlar el estado del negocio.

### Criterios de aceptación

- Debe mostrar tarjetas con:
  - Total prestado.
  - Total recaudado hoy.
  - Cartera activa.
  - Clientes en mora.
  - Rutas activas.
  - Trabajadores activos.
- Debe mostrar recaudo esperado vs real.
- Debe mostrar resumen por ruta.
- Debe permitir navegar a detalles.

---

## HU-FE-013 — Gestión de trabajadores

**Como** administrador,  
**quiero** administrar trabajadores,  
**para** controlar quién puede cobrar y crear préstamos.

### Criterios de aceptación

- Debe listar trabajadores.
- Debe permitir crear trabajador.
- Debe permitir editar trabajador.
- Debe permitir activar o inactivar.
- Debe permitir asignar usuario.
- Debe permitir configurar límites del trabajador.

---

## HU-FE-014 — Configuración de límites del trabajador

**Como** administrador,  
**quiero** configurar los límites de cada trabajador,  
**para** controlar sus préstamos en campo.

### Criterios de aceptación

- Debe permitir definir:
  - Monto máximo por préstamo.
  - Tasa mínima.
  - Tasa máxima.
  - Plazo máximo.
  - Puede crear cliente.
  - Puede crear préstamo.
  - Puede definir tasa.
- Debe validar valores obligatorios.
- Debe guardar configuración activa.
- Debe mostrar historial o estado actual de límites.

---

## HU-FE-015 — Gestión de rutas

**Como** administrador,  
**quiero** administrar rutas,  
**para** organizar los recorridos de cobro.

### Criterios de aceptación

- Debe listar rutas.
- Debe permitir crear ruta.
- Debe permitir editar ruta.
- Debe permitir inactivar ruta.
- Debe mostrar trabajador asignado.
- Debe permitir ver clientes de la ruta.

---

## HU-FE-016 — Ordenamiento de clientes en ruta

**Como** administrador,  
**quiero** reorganizar clientes dentro de una ruta,  
**para** ajustar el recorrido cuando sea necesario.

### Criterios de aceptación

- Debe mostrar los clientes en orden.
- Debe permitir mover un cliente hacia arriba.
- Debe permitir mover un cliente hacia abajo.
- Debe permitir insertar cliente antes o después de otro.
- Debe actualizar visualmente el orden.
- Debe guardar el cambio en backend.
- Debe mostrar mensaje de éxito o error.

---

## HU-FE-017 — Gestión de clientes

**Como** administrador,  
**quiero** consultar y administrar clientes,  
**para** revisar su información, préstamos y estado.

### Criterios de aceptación

- Debe listar clientes.
- Debe permitir buscar por nombre, documento o teléfono.
- Debe permitir filtrar por ruta.
- Debe permitir filtrar por estado.
- Debe permitir ver detalle del cliente.
- El detalle debe mostrar:
  - Datos personales.
  - Ruta.
  - Préstamos.
  - Pagos.
  - Estado actual.

---

## HU-FE-018 — Gestión de préstamos

**Como** administrador,  
**quiero** consultar los préstamos creados,  
**para** controlar la cartera activa.

### Criterios de aceptación

- Debe listar préstamos.
- Debe mostrar cliente, trabajador, ruta, monto, tasa, total, saldo y estado.
- Debe permitir filtrar por estado.
- Debe permitir filtrar por ruta.
- Debe permitir ver detalle del préstamo.
- El detalle debe mostrar cuotas y pagos asociados.

---

## HU-FE-019 — Consulta de pagos

**Como** administrador,  
**quiero** consultar los pagos registrados,  
**para** verificar el recaudo diario.

### Criterios de aceptación

- Debe listar pagos.
- Debe mostrar fecha, cliente, trabajador, ruta, valor y estado.
- Debe permitir filtrar por fecha.
- Debe permitir filtrar por trabajador.
- Debe permitir filtrar por ruta.
- Debe permitir ver detalle del pago.
- Debe permitir identificar pagos anulados o reversados si aplica.

---

## HU-FE-020 — Cierre de caja administrativo

**Como** administrador,  
**quiero** revisar y cerrar la caja diaria de un trabajador,  
**para** validar el dinero entregado.

### Criterios de aceptación

- Debe mostrar caja inicial.
- Debe mostrar préstamos entregados.
- Debe mostrar recaudo recibido.
- Debe mostrar valor esperado de cierre.
- Debe permitir ingresar valor entregado.
- Debe calcular diferencia.
- Si hay diferencia, debe solicitar observación.
- Debe permitir cerrar caja.
- No debe permitir cerrar una caja ya cerrada.

---

## HU-FE-021 — Reporte de clientes en mora

**Como** administrador,  
**quiero** ver los clientes en mora,  
**para** hacer seguimiento a la recuperación de cartera.

### Criterios de aceptación

- Debe listar clientes en mora.
- Debe mostrar:
  - Cliente.
  - Ruta.
  - Trabajador.
  - Días de mora.
  - Saldo pendiente.
- Debe permitir filtrar por ruta.
- Debe permitir filtrar por trabajador.
- Debe permitir filtrar por rango de días de mora.

---

## HU-FE-022 — Auditoría del sistema

**Como** administrador,  
**quiero** consultar las acciones realizadas en el sistema,  
**para** revisar trazabilidad y control interno.

### Criterios de aceptación

- Debe listar eventos de auditoría.
- Debe mostrar:
  - Fecha.
  - Usuario.
  - Acción.
  - Tabla afectada.
  - Registro afectado.
  - Observación.
- Debe permitir filtrar por usuario.
- Debe permitir filtrar por acción.
- Debe permitir filtrar por fecha.
- Debe permitir ver valor anterior y valor nuevo cuando aplique.
