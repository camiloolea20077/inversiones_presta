# Agente Backend — Sistema de Gestión de Préstamos y Recaudo Diario

## Rol del agente

Actúa como **Arquitecto Backend Senior** y **Desarrollador Java Spring Boot**.

Tu objetivo es construir el backend de un sistema de gestión de préstamos y recaudo diario, con una arquitectura limpia, mantenible, segura y preparada para crecimiento.

---

## Tecnologías obligatorias

- Java 17 o superior
- Spring Boot
- Spring Security con JWT
- PostgreSQL
- Spring Data JPA
- NamedParameterJdbcTemplate para QueryRepository
- MapStruct
- Bean Validation
- Lombok, si el proyecto lo permite
- Arquitectura por capas

---

## Arquitectura esperada

La estructura debe organizarse por módulos o dominios.

Ejemplo:

```text
src/main/java/com/app/recaudo
├── auth
├── roles
├── usuarios
├── trabajadores
├── rutas
├── clientes
├── ruta_clientes
├── limites_trabajador
├── prestamos
├── cuotas_prestamo
├── recaudos_diarios
├── pagos
├── cajas_diarias
├── movimientos_caja
├── auditoria
├── common
└── security
```

Cada módulo debe manejar, cuando aplique:

```text
controller
service
service.impl
repository
queryrepository
mapper
dto
entity
exception
```

---

## Convenciones generales

- Las tablas y columnas de base de datos están en español.
- Las clases pueden usar nombres en inglés o español, pero deben mapear correctamente a las tablas.
- Mantener nombres claros y consistentes.
- Usar DTOs para entrada y salida.
- No exponer entidades directamente en los controladores.
- Usar `@Transactional` en operaciones que modifiquen varias tablas.
- Usar borrado lógico con `deleted_at`.
- Usar auditoría: `created_at`, `updated_at`, `deleted_at`, `updated_by`.
- No eliminar registros físicamente.
- Manejar errores con excepciones controladas.
- Validar reglas de negocio en el service, no en el controller.

---

## Modelo de base de datos

Usar como base el archivo SQL:

```text
recaudo_diario_schema.sql
```

Tablas principales:

```text
roles
usuarios
trabajadores
rutas
ruta_trabajadores
clientes
ruta_clientes
limites_trabajador
prestamos
cuotas_prestamo
recaudos_diarios
recaudo_detalles
pagos
cajas_diarias
movimientos_caja
auditoria_general
```

---

## Módulos a construir

### 1. Autenticación y usuarios

Funcionalidades:

- Login
- Generación de JWT
- Validación de token
- Consulta de usuario autenticado
- Roles: ADMINISTRADOR, TRABAJADOR

Endpoints sugeridos:

```text
POST /api/auth/login
GET  /api/auth/me
```

---

### 2. Roles

Funcionalidades:

- Listar roles
- Crear rol, si aplica
- Activar/inactivar rol

Endpoints sugeridos:

```text
GET  /api/roles
POST /api/roles
PUT  /api/roles/{id}
DELETE /api/roles/{id}
```

---

### 3. Usuarios

Funcionalidades:

- Crear usuario
- Actualizar usuario
- Inactivar usuario
- Consultar usuarios
- Asociar usuario a trabajador

Endpoints sugeridos:

```text
POST /api/usuarios
GET  /api/usuarios/{id}
POST /api/usuarios/page
PUT  /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

---

### 4. Trabajadores

Funcionalidades:

- Crear trabajador
- Editar trabajador
- Inactivar trabajador
- Consultar trabajador
- Listar trabajadores activos

Endpoints sugeridos:

```text
POST /api/trabajadores
GET  /api/trabajadores/{id}
POST /api/trabajadores/page
PUT  /api/trabajadores/{id}
DELETE /api/trabajadores/{id}
GET  /api/trabajadores/activos
```

Reglas:

- Documento único para trabajadores activos.
- Puede estar asociado a un usuario.
- No eliminar físicamente.

---

### 5. Rutas

Funcionalidades:

- Crear ruta
- Editar ruta
- Inactivar ruta
- Listar rutas activas
- Consultar detalle de ruta

Endpoints sugeridos:

```text
POST /api/rutas
GET  /api/rutas/{id}
POST /api/rutas/page
PUT  /api/rutas/{id}
DELETE /api/rutas/{id}
GET  /api/rutas/activas
```

---

### 6. Asignación de trabajador a ruta

Funcionalidades:

- Asignar trabajador a ruta
- Cerrar asignación anterior
- Consultar ruta actual del trabajador
- Consultar historial de asignaciones

Endpoints sugeridos:

```text
POST /api/ruta-trabajadores/asignar
GET  /api/ruta-trabajadores/trabajador/{trabajadorId}/actual
GET  /api/ruta-trabajadores/ruta/{rutaId}/historial
```

Reglas:

- Una ruta debe tener máximo un trabajador activo asignado.
- Al asignar uno nuevo, cerrar la asignación activa anterior.
- Conservar historial.

---

### 7. Clientes

Funcionalidades:

- Crear cliente
- Editar cliente
- Consultar cliente
- Buscar cliente
- Inactivar/bloquear cliente
- Consultar préstamos y pagos del cliente

Endpoints sugeridos:

```text
POST /api/clientes
GET  /api/clientes/{id}
POST /api/clientes/page
PUT  /api/clientes/{id}
DELETE /api/clientes/{id}
PATCH /api/clientes/{id}/estado
GET  /api/clientes/{id}/resumen
```

Reglas:

- Nombre obligatorio.
- Documento opcional.
- Si el documento existe, debe permitir búsqueda.
- Estado inicial: ACTIVO.

---

### 8. Clientes por ruta

Funcionalidades:

- Agregar cliente a ruta.
- Insertar cliente después de otro cliente existente.
- Insertar cliente antes de otro cliente existente.
- Mover cliente arriba.
- Mover cliente abajo.
- Listar clientes de una ruta en orden.

Endpoints sugeridos:

```text
POST /api/ruta-clientes/agregar
POST /api/ruta-clientes/insertar-despues
POST /api/ruta-clientes/insertar-antes
PATCH /api/ruta-clientes/{id}/subir
PATCH /api/ruta-clientes/{id}/bajar
GET /api/ruta-clientes/ruta/{rutaId}
```

Reglas:

- No duplicar cliente activo en la misma ruta.
- No duplicar orden dentro de la misma ruta.
- Al insertar en medio, desplazar los órdenes posteriores.
- Registrar auditoría del cambio de orden.

Operación crítica:

```sql
UPDATE ruta_clientes
SET orden = orden + 1,
    updated_at = NOW(),
    updated_by = :usuarioId
WHERE ruta_id = :rutaId
  AND orden > :ordenBase
  AND deleted_at IS NULL
  AND activo = TRUE;
```

---

### 9. Límites del trabajador

Funcionalidades:

- Crear configuración de límites.
- Consultar límites activos.
- Actualizar límites.
- Inactivar límites anteriores.

Endpoints sugeridos:

```text
POST /api/limites-trabajador
GET  /api/limites-trabajador/trabajador/{trabajadorId}/activo
PUT  /api/limites-trabajador/{id}
DELETE /api/limites-trabajador/{id}
```

Reglas:

- Si un trabajador no tiene límites activos, no puede crear préstamos.
- Validar:
  - monto máximo
  - tasa mínima
  - tasa máxima
  - plazo máximo
  - puede crear cliente
  - puede crear préstamo
  - puede definir tasa

---

### 10. Préstamos

Funcionalidades:

- Crear préstamo desde ruta.
- Calcular interés.
- Calcular total a pagar.
- Calcular cuota diaria.
- Generar cuotas.
- Consultar préstamo.
- Consultar cartera activa.
- Anular préstamo, si aplica.

Endpoints sugeridos:

```text
POST /api/prestamos/simular
POST /api/prestamos/crear-desde-ruta
GET  /api/prestamos/{id}
POST /api/prestamos/page
PATCH /api/prestamos/{id}/anular
GET /api/prestamos/cliente/{clienteId}/activos
```

Reglas:

- Validar límites del trabajador.
- El préstamo queda ACTIVO inmediatamente.
- No requiere aprobación.
- Asociar cliente, ruta y trabajador.
- Generar cuotas automáticamente.
- Generar movimiento de caja PRESTAMO_ENTREGADO.
- Actualizar caja diaria del trabajador.

Cálculo base:

```text
valor_interes = monto_prestado * tasa_porcentaje / 100
total_pagar = monto_prestado + valor_interes
cuota_diaria = total_pagar / plazo_dias
```

La última cuota debe ajustar diferencias por redondeo.

---

### 11. Crear cliente y préstamo en una sola operación

Funcionalidad crítica.

Endpoint sugerido:

```text
POST /api/prestamos/crear-cliente-prestamo-ruta
```

Debe hacer en una sola transacción:

1. Crear cliente.
2. Insertarlo en la ruta después del cliente base.
3. Crear préstamo.
4. Generar cuotas.
5. Actualizar recaudo diario, si existe abierto.
6. Generar movimiento de caja PRESTAMO_ENTREGADO.
7. Actualizar caja diaria.
8. Registrar auditoría.

Si algo falla, se debe hacer rollback completo.

---

### 12. Recaudos diarios

Funcionalidades:

- Generar recaudo diario por ruta.
- Consultar recaudo diario de trabajador.
- Consultar detalle de ruta diaria.
- Actualizar totales.

Endpoints sugeridos:

```text
POST /api/recaudos-diarios/generar
GET  /api/recaudos-diarios/trabajador/{trabajadorId}/fecha/{fecha}
GET  /api/recaudos-diarios/{id}/detalle
PATCH /api/recaudos-diarios/{id}/cerrar
```

Reglas:

- No generar dos recaudos abiertos para la misma ruta, trabajador y fecha.
- Traer clientes activos de la ruta.
- Respetar orden de `ruta_clientes`.
- Asociar préstamo activo si existe.
- Si no hay préstamo, estado del detalle: SIN_PRESTAMO.

---

### 13. Pagos

Funcionalidades:

- Registrar pago total.
- Registrar pago parcial.
- Anular pago, si aplica.
- Consultar pagos por fecha, ruta, trabajador o cliente.

Endpoints sugeridos:

```text
POST /api/pagos
POST /api/pagos/page
GET  /api/pagos/{id}
PATCH /api/pagos/{id}/anular
GET  /api/pagos/cliente/{clienteId}
```

Reglas:

- No permitir pagos con valor cero o negativo.
- No permitir pagos sobre préstamo ANULADO.
- No permitir pagos sobre préstamo PAGADO.
- Actualizar cuota.
- Actualizar saldo del préstamo.
- Actualizar detalle de recaudo.
- Generar movimiento de caja PAGO_RECIBIDO.
- Actualizar caja diaria.
- Si el saldo llega a cero, marcar préstamo PAGADO.

---

### 14. Marcar no pagó

Endpoint sugerido:

```text
POST /api/recaudo-detalles/{id}/no-pago
```

Reglas:

- Actualizar estado del detalle a NO_PAGO.
- Guardar observación.
- No modificar saldo.
- No generar movimiento de caja.
- No cambiar orden del cliente.

---

### 15. Cajas diarias

Funcionalidades:

- Abrir caja.
- Consultar caja del trabajador.
- Actualizar totales.
- Cerrar caja.
- Consultar diferencia.

Endpoints sugeridos:

```text
POST /api/cajas-diarias/abrir
GET  /api/cajas-diarias/trabajador/{trabajadorId}/fecha/{fecha}
PATCH /api/cajas-diarias/{id}/cerrar
GET  /api/cajas-diarias/{id}/movimientos
```

Reglas:

- No abrir dos cajas para el mismo trabajador, ruta y fecha.
- Al abrir caja, crear movimiento CAJA_INICIAL.
- Al cerrar caja, calcular diferencia.
- No cerrar caja ya cerrada.

Fórmula:

```text
valor_esperado_cierre = valor_inicial - valor_prestamos_entregados + valor_recaudado
diferencia = valor_entregado - valor_esperado_cierre
```

---

### 16. Movimientos de caja

Funcionalidades:

- Registrar movimiento automático.
- Consultar movimientos por caja.
- Consultar movimientos por trabajador.
- Consultar movimientos por fecha.

Tipos:

```text
CAJA_INICIAL
PRESTAMO_ENTREGADO
PAGO_RECIBIDO
AJUSTE
CIERRE_CAJA
ANULACION_PAGO
ANULACION_PRESTAMO
```

---

### 17. Auditoría

Funcionalidades:

- Registrar auditoría automática.
- Consultar auditoría.
- Filtrar por usuario, fecha, acción y tabla.

Endpoints sugeridos:

```text
POST /api/auditoria/page
GET  /api/auditoria/{id}
```

Acciones:

```text
CREAR_CLIENTE
ACTUALIZAR_CLIENTE
CREAR_PRESTAMO
ANULAR_PRESTAMO
REGISTRAR_PAGO
ANULAR_PAGO
CERRAR_CAJA
CAMBIAR_ORDEN_RUTA
```

---

## Reglas de seguridad

- Todos los endpoints deben requerir JWT, excepto login.
- El trabajador solo debe acceder a su ruta, sus clientes y sus operaciones.
- El administrador puede consultar todo.
- Validar rol con `@PreAuthorize` cuando aplique.
- No confiar en IDs enviados desde frontend para permisos; validar contra usuario autenticado.

---

## Manejo de errores

Crear errores controlados para:

```text
CLIENTE_NO_EXISTE
RUTA_NO_EXISTE
TRABAJADOR_NO_EXISTE
PRESTAMO_NO_EXISTE
PRESTAMO_NO_ACTIVO
CAJA_NO_ABIERTA
CAJA_YA_CERRADA
LIMITE_TRABAJADOR_NO_CONFIGURADO
MONTO_SUPERA_LIMITE
TASA_FUERA_DE_RANGO
PLAZO_SUPERA_LIMITE
CLIENTE_YA_EXISTE_EN_RUTA
ORDEN_RUTA_DUPLICADO
RECAUDO_DIARIO_YA_EXISTE
PAGO_INVALIDO
```

---

## Archivos .http

Al finalizar cada módulo, crear archivo `.http` de pruebas.

Estilo:

```http
@port = 9001
@token = Bearer PEGAR_TOKEN_AQUI
@base = http://localhost:{{port}}/api/clientes

### Crear cliente
POST {{base}}
Authorization: {{token}}
Content-Type: application/json

{
  "nombre": "Juan Perez",
  "documento": "1000000001",
  "telefono": "3000000000",
  "direccion": "Calle 10 # 15 - 20",
  "barrio": "Kennedy"
}
```

---

## Orden de construcción recomendado

1. Configuración base del proyecto.
2. Seguridad JWT.
3. Roles y usuarios.
4. Trabajadores.
5. Rutas.
6. Clientes.
7. Ruta clientes.
8. Límites trabajador.
9. Préstamos.
10. Cuotas.
11. Cajas diarias.
12. Recaudos diarios.
13. Pagos.
14. Dashboard.
15. Auditoría.
16. Archivos `.http`.
17. Pruebas unitarias e integración.

---

## Primera tarea

Empieza creando:

1. Entidades de:
   - clientes
   - rutas
   - ruta_clientes

2. DTOs de:
   - crear cliente
   - actualizar cliente
   - respuesta cliente
   - agregar cliente a ruta
   - insertar cliente después

3. Repositories JPA.

4. QueryRepository para listar clientes de ruta en orden.

5. Services con validaciones.

6. Controllers REST.

7. Archivo `.http` para pruebas del módulo.
