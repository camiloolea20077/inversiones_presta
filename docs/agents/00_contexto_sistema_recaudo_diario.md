# Contexto Funcional — Sistema de Gestión de Préstamos y Recaudo Diario

## 1. Descripción general

El sistema permitirá administrar un negocio de préstamos con recaudo diario por rutas.  
Cada trabajador tendrá una ruta asignada. En esa ruta existirán clientes organizados en un orden específico de recorrido.

El trabajador podrá:

- Ver su ruta diaria desde el celular.
- Registrar pagos.
- Registrar pagos parciales.
- Marcar clientes como no pagó.
- Crear clientes nuevos durante la ruta.
- Insertar un cliente nuevo después de otro cliente existente.
- Crear préstamos directamente desde la ruta.
- Definir monto, tasa y plazo del préstamo.
- Ver la simulación del préstamo antes de crearlo.
- Crear el préstamo sin aprobación, siempre que cumpla los límites configurados.
- Ver resumen de su caja diaria.

El administrador podrá:

- Crear y administrar trabajadores.
- Crear y administrar rutas.
- Ver clientes.
- Ver préstamos.
- Ver pagos.
- Ver caja diaria.
- Configurar límites por trabajador.
- Consultar clientes en mora.
- Consultar auditoría.
- Ver dashboard general.

---

## 2. Regla central del negocio

El orden de los clientes en la ruta es operativo y debe mantenerse.

Cuando un trabajador registra pagos, los clientes no deben moverse de posición.  
El orden depende de la ruta, no del estado del pago.

Ejemplo:

```text
1. Cliente A    Pendiente
2. Cliente B    Pagado
3. Cliente C    Parcial
4. Cliente D    No pagó
```

Aunque el cliente 3 pague antes que el cliente 1, debe seguir ocupando la posición 3.

---

## 3. Inserción de clientes en medio de la ruta

Si el trabajador va en la ruta y después del cliente 5 encuentra un nuevo cliente en la misma calle, el sistema debe permitir insertarlo después del cliente 5.

Antes:

```text
1. Cliente A
2. Cliente B
3. Cliente C
4. Cliente D
5. Cliente E
6. Cliente F
7. Cliente G
```

Después:

```text
1. Cliente A
2. Cliente B
3. Cliente C
4. Cliente D
5. Cliente E
6. Cliente Nuevo
7. Cliente F
8. Cliente G
```

El nuevo cliente no debe ir automáticamente al final si su ubicación real corresponde a una posición intermedia.

---

## 4. Creación de cliente y préstamo desde ruta

El trabajador puede crear un cliente y un préstamo en una sola operación.

Debe ingresar:

### Datos del cliente

- Nombre
- Documento
- Teléfono
- Dirección
- Barrio
- Observación

### Datos del préstamo

- Monto prestado
- Tasa porcentaje
- Plazo en días
- Tipo de interés

El sistema debe calcular:

- Valor del interés
- Total a pagar
- Cuota diaria
- Fecha inicio
- Fecha final

El préstamo queda activo inmediatamente si cumple los límites configurados.

---

## 5. Control de límites por trabajador

Cada trabajador debe tener límites configurados.

Ejemplo:

```text
Monto máximo por préstamo: $1.000.000
Tasa mínima: 5%
Tasa máxima: 15%
Plazo máximo: 60 días
Puede crear cliente: Sí
Puede crear préstamo: Sí
Puede definir tasa: Sí
```

Si el trabajador intenta crear un préstamo fuera de esos límites, el sistema debe bloquear la operación.

No se requiere aprobación.  
La validación es automática.

---

## 6. Caja diaria del trabajador

Cada trabajador puede manejar dinero en campo.

La caja debe controlar:

```text
Caja inicial
- Préstamos entregados
+ Pagos recibidos
= Valor esperado al cierre
```

Fórmula:

```text
valor_esperado_cierre = valor_inicial - valor_prestamos_entregados + valor_recaudado
```

Cuando se crea un préstamo:

```text
Movimiento de caja: PRESTAMO_ENTREGADO
```

Cuando se registra un pago:

```text
Movimiento de caja: PAGO_RECIBIDO
```

Cuando se abre caja:

```text
Movimiento de caja: CAJA_INICIAL
```

Cuando se cierra caja:

```text
Movimiento de caja: CIERRE_CAJA
```

---

## 7. Estados principales

### Clientes

```text
ACTIVO
BLOQUEADO
RETIRADO
EN_MORA
```

### Préstamos

```text
ACTIVO
PAGADO
VENCIDO
ANULADO
REFINANCIADO
```

### Cuotas

```text
PENDIENTE
PAGADA
PARCIAL
VENCIDA
ANULADA
```

### Recaudo diario

```text
ABIERTO
EN_PROCESO
CERRADO
ANULADO
```

### Detalle de recaudo

```text
PENDIENTE
PAGADO
PARCIAL
NO_PAGO
SIN_PRESTAMO
```

### Caja diaria

```text
ABIERTA
CERRADA
CERRADA_CON_DIFERENCIA
ANULADA
```

---

## 8. Auditoría

Todas las operaciones importantes deben quedar auditadas:

- Creación de cliente
- Actualización de cliente
- Inserción de cliente en ruta
- Cambio de orden de ruta
- Creación de préstamo
- Anulación de préstamo
- Registro de pago
- Anulación de pago
- Apertura de caja
- Cierre de caja
- Configuración de límites del trabajador

Todas las tablas deben manejar:

```text
created_at
updated_at
deleted_at
updated_by
```

No se deben eliminar registros físicamente.  
Se debe usar borrado lógico con `deleted_at`.

---

## 9. Base de datos

La base de datos fue diseñada en español y pensada para PostgreSQL.

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

## 10. Enfoque técnico recomendado

### Backend

- Java 17 o superior
- Spring Boot
- Spring Security con JWT
- PostgreSQL
- JPA Repository
- QueryRepository con NamedParameterJdbcTemplate
- MapStruct
- DTOs separados por módulo
- Servicios transaccionales
- Auditoría
- Borrado lógico

### Frontend

- Angular 18
- PrimeNG 18
- TailwindCSS
- Formularios reactivos
- Componentes standalone
- Diseño mobile first para trabajador
- Dashboard administrativo para escritorio
- Servicios HTTP tipados
