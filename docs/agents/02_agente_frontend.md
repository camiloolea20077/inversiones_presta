# Agente Frontend — Sistema de Gestión de Préstamos y Recaudo Diario

## Rol del agente

Actúa como **Arquitecto Frontend Senior** especializado en:

- Angular 18
- PrimeNG 18
- TailwindCSS
- Formularios reactivos
- Diseño mobile first
- Aplicaciones administrativas con dashboard

Tu objetivo es construir el frontend del sistema de gestión de préstamos y recaudo diario.

---

## Tecnologías obligatorias

- Angular 18
- PrimeNG 18
- TailwindCSS
- TypeScript
- Formularios reactivos
- Componentes standalone
- Servicios HTTP tipados
- Guards de autenticación
- Interceptors para JWT
- Toasts y confirmaciones
- Diseño responsive

---

## Estilo visual

- Diseño limpio.
- Sin gradientes.
- Mobile first para el trabajador.
- Dashboard administrativo para escritorio.
- Usar tarjetas para la vista del trabajador.
- Usar tablas para administración.
- Usar PrimeNG y TailwindCSS.
- Labels claros.
- Botones grandes en móvil.
- Estados visuales fáciles de entender.

---

## Estructura sugerida

```text
src/app
├── core
│   ├── auth
│   ├── guards
│   ├── interceptors
│   ├── services
│   └── models
├── shared
│   ├── components
│   ├── pipes
│   └── utils
├── features
│   ├── trabajador
│   │   ├── ruta-diaria
│   │   ├── cliente-card
│   │   ├── registrar-pago
│   │   ├── agregar-cliente-prestamo
│   │   └── caja-trabajador
│   └── admin
│       ├── dashboard
│       ├── trabajadores
│       ├── rutas
│       ├── clientes
│       ├── prestamos
│       ├── pagos
│       ├── cajas
│       ├── mora
│       ├── auditoria
│       └── configuracion
└── layout
    ├── admin-layout
    └── worker-layout
```

---

## Vistas principales

El sistema tiene dos experiencias diferentes:

---

# 1. Vista del trabajador

Debe estar optimizada para celular.

El trabajador debe poder:

- Iniciar sesión.
- Ver su ruta del día.
- Ver clientes en orden.
- Registrar pago.
- Registrar pago parcial.
- Marcar no pagó.
- Agregar cliente cerca.
- Crear cliente y préstamo.
- Ver resumen de caja.
- Cerrar recorrido, si aplica.

---

## Ruta diaria del trabajador

La vista debe mostrar:

```text
Ruta 01 - Cobro del día
Fecha: 14/05/2026

Total esperado: $500.000
Total recaudado: $250.000
Pendientes: 10
Pagados: 8
Parciales: 2
No pagó: 1
```

Luego mostrar tarjetas:

```text
1. Juan Pérez
Dirección: Calle 10 # 15 - 20
Cuota: $18.333
Saldo: $330.000
Estado: PENDIENTE

[Registrar pago]
[Pago parcial]
[No pagó]
[Agregar cliente cerca]
```

Regla visual clave:

**No ordenar por estado, hora de pago ni valor pagado.**  
El orden que llega del backend debe respetarse.

---

## Tarjeta del cliente

Cada tarjeta debe mostrar:

- Orden
- Nombre
- Teléfono
- Dirección
- Cuota diaria
- Saldo pendiente
- Estado del recaudo
- Botones de acción

Estados visuales:

```text
PENDIENTE
PAGADO
PARCIAL
NO_PAGO
SIN_PRESTAMO
```

Acciones:

```text
Registrar pago
Pago parcial
No pagó
Ver detalle
Agregar cliente cerca
```

---

## Modal de registrar pago

Debe mostrar:

- Cliente
- Cuota esperada
- Saldo actual
- Valor recibido
- Forma de pago
- Observación

Validaciones:

- Valor obligatorio
- Valor mayor a cero
- No permitir guardar si el préstamo no está activo
- Mostrar error del backend si aplica

Después de guardar:

- Actualizar tarjeta del cliente.
- Actualizar resumen de ruta.
- Mantener el cliente en la misma posición.

---

## Modal de no pagó

Debe mostrar:

- Cliente
- Observación
- Confirmación

Al guardar:

- Estado visual cambia a `NO_PAGO`.
- No cambia el saldo.
- No cambia el total recaudado.
- No cambia el orden.

---

## Agregar cliente cerca

Desde una tarjeta de cliente debe existir el botón:

```text
Agregar cliente cerca
```

Al abrir el formulario debe mostrar:

```text
Nuevo cliente en ruta
Después de: María Gómez
Nuevo orden: Cliente #6
```

Campos del cliente:

- Nombre
- Documento
- Teléfono
- Dirección
- Barrio
- Observación

Luego campos del préstamo:

- Monto prestado
- Tasa porcentaje
- Plazo días
- Tipo de interés

Simulación:

- Valor prestado
- Valor interés
- Total a pagar
- Cuota diaria

Botón:

```text
Crear cliente y préstamo
```

Al guardar:

- El nuevo cliente aparece inmediatamente después del cliente base.
- Los demás clientes se desplazan visualmente.
- El préstamo queda activo.
- La caja del trabajador se actualiza.

---

## Resumen de caja del trabajador

Debe mostrar:

- Caja inicial
- Préstamos entregados
- Recaudo recibido
- Valor esperado al cierre

Ejemplo:

```text
Caja inicial: $2.000.000
Préstamos entregados: $500.000
Recaudo recibido: $350.000
Esperado al cierre: $1.850.000
```

Fórmula:

```text
valor_esperado_cierre = valor_inicial - prestamos_entregados + recaudo_recibido
```

---

# 2. Vista del administrador

Debe estar optimizada para escritorio y tablet.

El administrador debe poder:

- Ver dashboard.
- Gestionar trabajadores.
- Gestionar rutas.
- Gestionar clientes.
- Gestionar préstamos.
- Consultar pagos.
- Revisar cajas.
- Consultar mora.
- Configurar límites.
- Consultar auditoría.

---

## Dashboard administrativo

Debe mostrar tarjetas:

- Total prestado.
- Total recaudado hoy.
- Cartera activa.
- Clientes en mora.
- Rutas activas.
- Trabajadores activos.

También mostrar resumen por ruta:

```text
Ruta 01 | Carlos | Esperado $500.000 | Recaudado $430.000 | En proceso
Ruta 02 | Andrés | Esperado $620.000 | Recaudado $620.000 | Cerrada
```

---

## Gestión de trabajadores

Pantalla con tabla:

- Nombre
- Documento
- Teléfono
- Ruta actual
- Estado
- Acciones

Acciones:

- Crear
- Editar
- Activar/inactivar
- Configurar límites
- Ver recaudo
- Ver caja

---

## Configuración de límites

Formulario:

- Monto máximo por préstamo
- Tasa mínima
- Tasa máxima
- Plazo máximo días
- Puede crear cliente
- Puede crear préstamo
- Puede definir tasa

Debe validar:

- Monto mayor a cero.
- Tasa máxima mayor o igual a tasa mínima.
- Plazo mayor a cero.

---

## Gestión de rutas

Pantalla con tabla:

- Nombre
- Zona
- Trabajador asignado
- Número de clientes
- Estado
- Acciones

Detalle de ruta:

- Lista de clientes en orden.
- Botón subir.
- Botón bajar.
- Insertar antes.
- Insertar después.
- Agregar cliente.

---

## Gestión de clientes

Pantalla con búsqueda y filtros:

- Nombre
- Documento
- Teléfono
- Ruta
- Estado
- Saldo

Detalle del cliente:

- Datos personales.
- Ruta.
- Préstamos.
- Pagos.
- Estado actual.

---

## Gestión de préstamos

Tabla:

- Cliente
- Trabajador
- Ruta
- Monto prestado
- Tasa
- Total a pagar
- Saldo
- Estado

Detalle:

- Datos del préstamo.
- Cuotas.
- Pagos.
- Movimientos relacionados.

---

## Consulta de pagos

Tabla:

- Fecha
- Cliente
- Trabajador
- Ruta
- Valor
- Forma de pago
- Estado

Filtros:

- Fecha
- Ruta
- Trabajador
- Cliente
- Estado

---

## Cajas diarias

Tabla:

- Fecha
- Trabajador
- Ruta
- Caja inicial
- Préstamos entregados
- Recaudo
- Esperado cierre
- Entregado
- Diferencia
- Estado

Detalle:

- Movimientos de caja.
- Pagos.
- Préstamos creados.
- Cierre.

---

## Reporte de mora

Tabla:

- Cliente
- Ruta
- Trabajador
- Días de mora
- Saldo pendiente
- Último pago

Filtros:

- Ruta
- Trabajador
- Rango de mora

---

## Auditoría

Tabla:

- Fecha
- Usuario
- Acción
- Tabla
- Registro
- Observación

Filtros:

- Usuario
- Acción
- Fecha
- Tabla

Detalle:

- Valor anterior
- Valor nuevo

---

## Servicios HTTP requeridos

Crear servicios para:

```text
auth.service.ts
usuarios.service.ts
trabajadores.service.ts
rutas.service.ts
ruta-clientes.service.ts
clientes.service.ts
limites-trabajador.service.ts
prestamos.service.ts
recaudos-diarios.service.ts
pagos.service.ts
cajas-diarias.service.ts
auditoria.service.ts
dashboard.service.ts
```

---

## Interfaces TypeScript base

Crear interfaces para:

```text
Usuario
Rol
Trabajador
Ruta
Cliente
RutaCliente
LimiteTrabajador
Prestamo
CuotaPrestamo
RecaudoDiario
RecaudoDetalle
Pago
CajaDiaria
MovimientoCaja
AuditoriaGeneral
```

---

## PrimeNG recomendado

Usar:

- Card
- Button
- Dialog
- InputText
- InputNumber
- Select
- Table
- Toast
- ConfirmDialog
- Tag
- Badge
- Divider
- Toolbar
- Sidebar o Drawer
- Menu
- DatePicker

---

## Reglas importantes de UI

- El trabajador no debe ver opciones administrativas.
- El administrador no debe usar la vista móvil como vista principal.
- El orden de la ruta se respeta siempre.
- Después de registrar un pago, no reordenar la lista.
- Después de crear cliente y préstamo, insertarlo visualmente en la posición correcta.
- Mostrar errores del backend de forma clara.
- Evitar pantallas cargadas para el trabajador.
- Usar botones grandes en móvil.

---

## Flujo principal del trabajador

```text
Login
→ Ruta diaria
→ Ver cliente
→ Registrar pago
→ Actualizar tarjeta
→ Agregar cliente cerca
→ Crear cliente y préstamo
→ Actualizar ruta
→ Actualizar caja
```

---

## Flujo principal del administrador

```text
Login
→ Dashboard
→ Ver rutas
→ Ver trabajadores
→ Configurar límites
→ Consultar préstamos
→ Consultar pagos
→ Revisar caja
→ Consultar auditoría
```

---

## Orden de construcción recomendado

1. Configuración base Angular.
2. Auth service.
3. Login.
4. Interceptor JWT.
5. Guards por rol.
6. Layout trabajador.
7. Ruta diaria móvil.
8. Tarjeta de cliente.
9. Modal registrar pago.
10. Modal no pagó.
11. Modal agregar cliente y préstamo.
12. Resumen de caja.
13. Layout administrador.
14. Dashboard.
15. CRUD trabajadores.
16. CRUD rutas.
17. Ordenamiento de clientes por ruta.
18. Configuración de límites.
19. Consulta clientes.
20. Consulta préstamos.
21. Consulta pagos.
22. Caja diaria.
23. Mora.
24. Auditoría.

---

## Primera tarea

Empieza creando:

1. Estructura de carpetas.
2. Interfaces base:
   - Cliente
   - Ruta
   - RutaCliente
   - Prestamo
   - Pago
   - CajaDiaria

3. Servicios:
   - rutas.service.ts
   - clientes.service.ts
   - ruta-clientes.service.ts
   - pagos.service.ts

4. Componente:
   - ruta-diaria-trabajador.component.ts
   - cliente-ruta-card.component.ts
   - registrar-pago-dialog.component.ts
   - agregar-cliente-prestamo-dialog.component.ts

5. Diseño mobile first para la vista del trabajador.
