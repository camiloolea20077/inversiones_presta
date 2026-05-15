# Backlog MVP Priorizado — Sistema de Recaudo Diario

Este archivo define el orden recomendado para construir el MVP del sistema.

---

## Objetivo del MVP

Construir primero el flujo mínimo funcional:

```text
Trabajador inicia sesión
→ ve su ruta diaria
→ registra pagos
→ agrega cliente en medio de la ruta
→ crea cliente y préstamo
→ el préstamo afecta la caja
→ el administrador controla rutas, pagos, préstamos y caja
```

---

## Fase 1 — Base del sistema

### Backend

1. HU-BE-001 — Gestión de roles y usuarios
2. HU-BE-002 — Autenticación con JWT
3. HU-BE-003 — Gestión de trabajadores
4. HU-BE-004 — Gestión de rutas
5. HU-BE-005 — Asignación de trabajador a ruta

### Frontend

1. HU-FE-001 — Login del sistema
2. HU-FE-002 — Layout para administrador
3. HU-FE-003 — Layout móvil para trabajador

---

## Fase 2 — Clientes y rutas

### Backend

6. HU-BE-006 — Gestión de clientes
7. HU-BE-007 — Insertar cliente en una ruta con orden específico

### Frontend

4. HU-FE-004 — Vista de ruta diaria del trabajador
5. HU-FE-005 — Tarjeta de cliente en ruta
9. HU-FE-009 — Agregar cliente cerca
15. HU-FE-015 — Gestión de rutas
16. HU-FE-016 — Ordenamiento de clientes en ruta
17. HU-FE-017 — Gestión de clientes

---

## Fase 3 — Préstamos

### Backend

8. HU-BE-008 — Configuración de límites por trabajador
9. HU-BE-009 — Crear préstamo desde la ruta
10. HU-BE-010 — Generación automática de cuotas del préstamo
14. HU-BE-014 — Crear cliente y préstamo en una sola operación

### Frontend

10. HU-FE-010 — Crear cliente y préstamo desde ruta
13. HU-FE-013 — Gestión de trabajadores
14. HU-FE-014 — Configuración de límites del trabajador
18. HU-FE-018 — Gestión de préstamos

---

## Fase 4 — Recaudo diario y pagos

### Backend

11. HU-BE-011 — Generar recaudo diario por ruta
12. HU-BE-012 — Registrar pago de cliente
13. HU-BE-013 — Marcar cliente como no pagó
17. HU-BE-017 — Consulta de ruta diaria del trabajador

### Frontend

6. HU-FE-006 — Registrar pago
7. HU-FE-007 — Registrar pago parcial
8. HU-FE-008 — Marcar cliente como no pagó
19. HU-FE-019 — Consulta de pagos

---

## Fase 5 — Caja diaria

### Backend

15. HU-BE-015 — Control de caja diaria del trabajador
16. HU-BE-016 — Movimientos de caja

### Frontend

11. HU-FE-011 — Resumen de caja del trabajador
20. HU-FE-020 — Cierre de caja administrativo

---

## Fase 6 — Administración, reportes y auditoría

### Backend

18. HU-BE-018 — Dashboard administrativo
19. HU-BE-019 — Reporte de clientes en mora
20. HU-BE-020 — Auditoría general

### Frontend

12. HU-FE-012 — Dashboard administrativo
21. HU-FE-021 — Reporte de clientes en mora
22. HU-FE-022 — Auditoría del sistema

---

## MVP mínimo para primera demo

Para una primera demo funcional, construir:

### Backend mínimo

```text
HU-BE-001
HU-BE-002
HU-BE-003
HU-BE-004
HU-BE-005
HU-BE-006
HU-BE-007
HU-BE-008
HU-BE-009
HU-BE-010
HU-BE-011
HU-BE-012
HU-BE-014
HU-BE-015
HU-BE-016
HU-BE-017
```

### Frontend mínimo

```text
HU-FE-001
HU-FE-003
HU-FE-004
HU-FE-005
HU-FE-006
HU-FE-007
HU-FE-008
HU-FE-009
HU-FE-010
HU-FE-011
HU-FE-012
HU-FE-013
HU-FE-014
HU-FE-015
HU-FE-016
```

---

## Secuencia técnica sugerida

1. Crear base del backend.
2. Configurar seguridad JWT.
3. Crear usuarios, roles y trabajadores.
4. Crear rutas.
5. Asignar trabajador a ruta.
6. Crear clientes.
7. Insertar clientes con orden.
8. Crear límites del trabajador.
9. Crear préstamos.
10. Generar cuotas.
11. Crear caja diaria.
12. Generar recaudo diario.
13. Registrar pagos.
14. Actualizar caja.
15. Crear vista móvil trabajador.
16. Crear dashboard administrador.
17. Crear reportes.
18. Crear auditoría.
