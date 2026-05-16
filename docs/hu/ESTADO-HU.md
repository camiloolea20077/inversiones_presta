# Estado de las Historias de Usuario

Control de avance del Sistema de Recaudo Diario. Estados: ✅ listo · 🟡 parcial · ⬜ pendiente.

> Lo actualiza el agente `implementar-hu` cada vez que se completa una HU.

## Backend (HU-BE)

| HU | Descripción | Estado |
|----|-------------|--------|
| HU-BE-001 | Gestión de roles y usuarios | ✅ |
| HU-BE-002 | Autenticación con JWT | ✅ |
| HU-BE-003 | Gestión de trabajadores | ✅ |
| HU-BE-004 | Gestión de rutas | ✅ |
| HU-BE-005 | Asignación de trabajador a ruta | ✅ |
| HU-BE-006 | Gestión de clientes | ✅ |
| HU-BE-007 | Insertar cliente en ruta con orden | ✅ |
| HU-BE-008 | Configuración de límites por trabajador | ✅ |
| HU-BE-009 | Crear préstamo desde la ruta | ✅ |
| HU-BE-010 | Generación automática de cuotas | ✅ |
| HU-BE-011 | Generar recaudo diario por ruta | ✅ |
| HU-BE-012 | Registrar pago de cliente | ✅ |
| HU-BE-013 | Marcar cliente como no pagó | ✅ |
| HU-BE-014 | Crear cliente y préstamo en una sola operación | ⬜ |
| HU-BE-015 | Control de caja diaria del trabajador | ⬜ |
| HU-BE-016 | Movimientos de caja | ⬜ |
| HU-BE-017 | Consulta de ruta diaria del trabajador | ✅ |
| HU-BE-018 | Dashboard administrativo | ⬜ |
| HU-BE-019 | Reporte de clientes en mora | ⬜ |
| HU-BE-020 | Auditoría general | ⬜ |

## Frontend (HU-FE)

| HU | Descripción | Estado |
|----|-------------|--------|
| HU-FE-001 | Login del sistema | ✅ |
| HU-FE-002 | Layout para administrador | ✅ |
| HU-FE-003 | Layout móvil para trabajador | ✅ |
| HU-FE-004 | Vista de ruta diaria del trabajador | ✅ |
| HU-FE-005 | Tarjeta de cliente en ruta | ✅ |
| HU-FE-006 | Registrar pago | ✅ |
| HU-FE-007 | Registrar pago parcial | ✅ |
| HU-FE-008 | Marcar cliente como no pagó | ✅ |
| HU-FE-009 | Agregar cliente cerca | ⬜ |
| HU-FE-010 | Crear cliente y préstamo desde ruta | ⬜ |
| HU-FE-011 | Resumen de caja del trabajador | ⬜ |
| HU-FE-012 | Dashboard administrativo | 🟡 (data quemada; falta conectar HU-BE-018) |
| HU-FE-013 | Gestión de trabajadores | ✅ |
| HU-FE-014 | Configuración de límites del trabajador | ✅ |
| HU-FE-015 | Gestión de rutas | ✅ |
| HU-FE-016 | Ordenamiento de clientes en ruta | ✅ |
| HU-FE-017 | Gestión de clientes | ✅ |
| HU-FE-018 | Gestión de préstamos | ✅ |
| HU-FE-019 | Consulta de pagos | ⬜ |
| HU-FE-020 | Cierre de caja administrativo | ⬜ |
| HU-FE-021 | Reporte de clientes en mora | ⬜ |
| HU-FE-022 | Auditoría del sistema | ⬜ |

## Pendientes técnicos

- Movimiento de caja `PAGO_RECIBIDO`: hay un `TODO` en `PagoServiceImpl` — se conecta al implementar HU-BE-015/016.
