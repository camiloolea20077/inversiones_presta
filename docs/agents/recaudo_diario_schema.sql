/*
============================================================
 SISTEMA DE GESTIÓN DE PRÉSTAMOS Y RECAUDO DIARIO
 Base de datos PostgreSQL
 Autor: generado para proyecto de recaudo diario
 Fecha base: 2026-05-14
============================================================

 Este archivo crea la estructura inicial del sistema:
 - Usuarios, roles y trabajadores
 - Rutas y clientes en orden de recorrido
 - Préstamos, cuotas y pagos
 - Recaudos diarios
 - Caja diaria del trabajador
 - Movimientos de caja
 - Auditoría general

 Convención de auditoría en todas las tablas:
 - created_at
 - updated_at
 - deleted_at
 - updated_by

 Nota:
 - deleted_at se usa para borrado lógico.
 - updated_by guarda el ID del usuario que realizó la última modificación.
============================================================
*/

BEGIN;

-- ============================================================
-- LIMPIEZA OPCIONAL PARA AMBIENTE DE DESARROLLO
-- Si ya tienes datos reales, NO ejecutes esta sección.
-- ============================================================

DROP TABLE IF EXISTS auditoria_general CASCADE;
DROP TABLE IF EXISTS movimientos_presupuesto CASCADE;
DROP TABLE IF EXISTS presupuesto_admin CASCADE;
DROP TABLE IF EXISTS movimientos_caja CASCADE;
DROP TABLE IF EXISTS cajas_diarias CASCADE;
DROP TABLE IF EXISTS pagos CASCADE;
DROP TABLE IF EXISTS recaudo_detalles CASCADE;
DROP TABLE IF EXISTS recaudos_diarios CASCADE;
DROP TABLE IF EXISTS cuotas_prestamo CASCADE;
DROP TABLE IF EXISTS prestamos CASCADE;
DROP TABLE IF EXISTS limites_trabajador CASCADE;
DROP TABLE IF EXISTS ruta_clientes CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS ruta_trabajadores CASCADE;
DROP TABLE IF EXISTS rutas CASCADE;
DROP TABLE IF EXISTS trabajadores CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

DROP FUNCTION IF EXISTS fn_set_updated_at() CASCADE;
DROP FUNCTION IF EXISTS fn_insertar_cliente_en_ruta(BIGINT, BIGINT, INTEGER, BIGINT) CASCADE;

-- ============================================================
-- FUNCIÓN GENERAL PARA ACTUALIZAR updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 1. ROLES
-- ============================================================

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT
);

CREATE UNIQUE INDEX uq_roles_nombre_activo
ON roles (nombre)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_roles_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 2. USUARIOS
-- ============================================================

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL,

    nombre VARCHAR(150) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_usuarios_roles
        FOREIGN KEY (rol_id) REFERENCES roles(id)
);

CREATE UNIQUE INDEX uq_usuarios_usuario_activo
ON usuarios (usuario)
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_usuarios_email_activo
ON usuarios (email)
WHERE email IS NOT NULL AND deleted_at IS NULL;

CREATE TRIGGER trg_usuarios_updated_at
BEFORE UPDATE ON usuarios
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 3. TRABAJADORES
-- ============================================================

CREATE TABLE trabajadores (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,

    documento VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_trabajadores_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE UNIQUE INDEX uq_trabajadores_documento_activo
ON trabajadores (documento)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_trabajadores_updated_at
BEFORE UPDATE ON trabajadores
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 4. RUTAS
-- ============================================================

CREATE TABLE rutas (
    id BIGSERIAL PRIMARY KEY,

    nombre VARCHAR(100) NOT NULL,
    zona VARCHAR(150),
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT
);

CREATE UNIQUE INDEX uq_rutas_nombre_activo
ON rutas (nombre)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_rutas_updated_at
BEFORE UPDATE ON rutas
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 5. RUTA_TRABAJADORES
-- Permite cambiar el trabajador asignado a una ruta sin perder historial.
-- ============================================================

CREATE TABLE ruta_trabajadores (
    id BIGSERIAL PRIMARY KEY,

    ruta_id BIGINT NOT NULL,
    trabajador_id BIGINT NOT NULL,

    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_ruta_trabajadores_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT fk_ruta_trabajadores_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT chk_ruta_trabajadores_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX idx_ruta_trabajadores_ruta
ON ruta_trabajadores (ruta_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_ruta_trabajadores_trabajador
ON ruta_trabajadores (trabajador_id)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_ruta_trabajadores_updated_at
BEFORE UPDATE ON ruta_trabajadores
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 6. CLIENTES
-- ============================================================

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,

    documento VARCHAR(30),
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    barrio VARCHAR(150),
    observacion TEXT,

    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT chk_clientes_estado
        CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'RETIRADO', 'EN_MORA'))
);

CREATE INDEX idx_clientes_nombre
ON clientes (nombre)
WHERE deleted_at IS NULL;

CREATE INDEX idx_clientes_documento
ON clientes (documento)
WHERE deleted_at IS NULL;

CREATE INDEX idx_clientes_telefono
ON clientes (telefono)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_clientes_updated_at
BEFORE UPDATE ON clientes
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 7. RUTA_CLIENTES
-- Guarda el orden real del cliente dentro de la ruta.
-- ============================================================

CREATE TABLE ruta_clientes (
    id BIGSERIAL PRIMARY KEY,

    ruta_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,

    orden INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_ruta_clientes_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT fk_ruta_clientes_clientes
        FOREIGN KEY (cliente_id) REFERENCES clientes(id),

    CONSTRAINT chk_ruta_clientes_orden
        CHECK (orden > 0)
);

CREATE UNIQUE INDEX uq_ruta_clientes_orden_activo
ON ruta_clientes (ruta_id, orden)
WHERE deleted_at IS NULL AND activo = TRUE;

CREATE UNIQUE INDEX uq_ruta_clientes_cliente_activo
ON ruta_clientes (ruta_id, cliente_id)
WHERE deleted_at IS NULL AND activo = TRUE;

CREATE INDEX idx_ruta_clientes_ruta_orden
ON ruta_clientes (ruta_id, orden)
WHERE deleted_at IS NULL AND activo = TRUE;

CREATE TRIGGER trg_ruta_clientes_updated_at
BEFORE UPDATE ON ruta_clientes
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 8. LIMITES_TRABAJADOR
-- Controla cuánto puede prestar cada trabajador.
-- ============================================================

CREATE TABLE limites_trabajador (
    id BIGSERIAL PRIMARY KEY,

    trabajador_id BIGINT NOT NULL,

    monto_maximo_prestamo NUMERIC(18,2) NOT NULL DEFAULT 0,
    tasa_minima NUMERIC(8,4) NOT NULL DEFAULT 0,
    tasa_maxima NUMERIC(8,4) NOT NULL DEFAULT 0,
    plazo_maximo_dias INTEGER NOT NULL DEFAULT 0,

    puede_crear_cliente BOOLEAN NOT NULL DEFAULT TRUE,
    puede_crear_prestamo BOOLEAN NOT NULL DEFAULT TRUE,
    puede_definir_tasa BOOLEAN NOT NULL DEFAULT TRUE,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_limites_trabajador_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT chk_limites_trabajador_monto
        CHECK (monto_maximo_prestamo >= 0),

    CONSTRAINT chk_limites_trabajador_tasas
        CHECK (tasa_minima >= 0 AND tasa_maxima >= tasa_minima),

    CONSTRAINT chk_limites_trabajador_plazo
        CHECK (plazo_maximo_dias >= 0)
);

CREATE UNIQUE INDEX uq_limites_trabajador_activo
ON limites_trabajador (trabajador_id)
WHERE deleted_at IS NULL AND activo = TRUE;

CREATE TRIGGER trg_limites_trabajador_updated_at
BEFORE UPDATE ON limites_trabajador
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 9. PRESTAMOS
-- ============================================================

CREATE TABLE prestamos (
    id BIGSERIAL PRIMARY KEY,

    cliente_id BIGINT NOT NULL,
    ruta_id BIGINT NOT NULL,
    trabajador_id BIGINT NOT NULL,

    monto_prestado NUMERIC(18,2) NOT NULL,
    tasa_porcentaje NUMERIC(8,4) NOT NULL,
    tipo_interes VARCHAR(50) NOT NULL DEFAULT 'FIJO_PRESTAMO',

    plazo_dias INTEGER NOT NULL,
    valor_interes NUMERIC(18,2) NOT NULL,
    total_pagar NUMERIC(18,2) NOT NULL,
    cuota_diaria NUMERIC(18,2) NOT NULL,
    saldo_actual NUMERIC(18,2) NOT NULL,

    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_prestamos_clientes
        FOREIGN KEY (cliente_id) REFERENCES clientes(id),

    CONSTRAINT fk_prestamos_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT fk_prestamos_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT chk_prestamos_montos
        CHECK (
            monto_prestado > 0
            AND tasa_porcentaje >= 0
            AND plazo_dias > 0
            AND valor_interes >= 0
            AND total_pagar >= monto_prestado
            AND cuota_diaria > 0
            AND saldo_actual >= 0
        ),

    CONSTRAINT chk_prestamos_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT chk_prestamos_estado
        CHECK (estado IN ('ACTIVO', 'PAGADO', 'VENCIDO', 'ANULADO', 'REFINANCIADO')),

    CONSTRAINT chk_prestamos_tipo_interes
        CHECK (tipo_interes IN ('FIJO_PRESTAMO', 'MENSUAL', 'DIARIO'))
);

CREATE INDEX idx_prestamos_cliente
ON prestamos (cliente_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_prestamos_ruta
ON prestamos (ruta_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_prestamos_trabajador
ON prestamos (trabajador_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_prestamos_estado
ON prestamos (estado)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_prestamos_updated_at
BEFORE UPDATE ON prestamos
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 10. CUOTAS_PRESTAMO
-- ============================================================

CREATE TABLE cuotas_prestamo (
    id BIGSERIAL PRIMARY KEY,

    prestamo_id BIGINT NOT NULL,

    numero_cuota INTEGER NOT NULL,
    fecha_cuota DATE NOT NULL,

    valor_cuota NUMERIC(18,2) NOT NULL,
    valor_pagado NUMERIC(18,2) NOT NULL DEFAULT 0,
    saldo_cuota NUMERIC(18,2) NOT NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_cuotas_prestamo_prestamos
        FOREIGN KEY (prestamo_id) REFERENCES prestamos(id),

    CONSTRAINT chk_cuotas_prestamo_numero
        CHECK (numero_cuota > 0),

    CONSTRAINT chk_cuotas_prestamo_valores
        CHECK (valor_cuota > 0 AND valor_pagado >= 0 AND saldo_cuota >= 0),

    CONSTRAINT chk_cuotas_prestamo_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADA', 'PARCIAL', 'VENCIDA', 'ANULADA'))
);

CREATE UNIQUE INDEX uq_cuotas_prestamo_numero
ON cuotas_prestamo (prestamo_id, numero_cuota)
WHERE deleted_at IS NULL;

CREATE INDEX idx_cuotas_prestamo_fecha
ON cuotas_prestamo (fecha_cuota)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_cuotas_prestamo_updated_at
BEFORE UPDATE ON cuotas_prestamo
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 11. RECAUDOS_DIARIOS
-- Planilla diaria por ruta y trabajador.
-- ============================================================

CREATE TABLE recaudos_diarios (
    id BIGSERIAL PRIMARY KEY,

    ruta_id BIGINT NOT NULL,
    trabajador_id BIGINT NOT NULL,

    fecha_recaudo DATE NOT NULL,
    total_esperado NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_recaudado NUMERIC(18,2) NOT NULL DEFAULT 0,

    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_recaudos_diarios_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT fk_recaudos_diarios_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT chk_recaudos_diarios_totales
        CHECK (total_esperado >= 0 AND total_recaudado >= 0),

    CONSTRAINT chk_recaudos_diarios_estado
        CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'CERRADO', 'ANULADO'))
);

CREATE UNIQUE INDEX uq_recaudos_diarios_ruta_fecha
ON recaudos_diarios (ruta_id, fecha_recaudo)
WHERE deleted_at IS NULL AND estado <> 'ANULADO';

CREATE INDEX idx_recaudos_diarios_trabajador_fecha
ON recaudos_diarios (trabajador_id, fecha_recaudo)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_recaudos_diarios_updated_at
BEFORE UPDATE ON recaudos_diarios
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 12. RECAUDO_DETALLES
-- Mantiene el orden de la ruta en la planilla diaria.
-- ============================================================

CREATE TABLE recaudo_detalles (
    id BIGSERIAL PRIMARY KEY,

    recaudo_diario_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    prestamo_id BIGINT,

    orden INTEGER NOT NULL,

    valor_esperado NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_pagado NUMERIC(18,2) NOT NULL DEFAULT 0,
    saldo_pendiente NUMERIC(18,2) NOT NULL DEFAULT 0,

    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_recaudo_detalles_recaudos
        FOREIGN KEY (recaudo_diario_id) REFERENCES recaudos_diarios(id),

    CONSTRAINT fk_recaudo_detalles_clientes
        FOREIGN KEY (cliente_id) REFERENCES clientes(id),

    CONSTRAINT fk_recaudo_detalles_prestamos
        FOREIGN KEY (prestamo_id) REFERENCES prestamos(id),

    CONSTRAINT chk_recaudo_detalles_orden
        CHECK (orden > 0),

    CONSTRAINT chk_recaudo_detalles_valores
        CHECK (valor_esperado >= 0 AND valor_pagado >= 0 AND saldo_pendiente >= 0),

    CONSTRAINT chk_recaudo_detalles_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADO', 'PARCIAL', 'NO_PAGO', 'SIN_PRESTAMO'))
);

CREATE UNIQUE INDEX uq_recaudo_detalles_orden
ON recaudo_detalles (recaudo_diario_id, orden)
WHERE deleted_at IS NULL;

CREATE INDEX idx_recaudo_detalles_cliente
ON recaudo_detalles (cliente_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_recaudo_detalles_estado
ON recaudo_detalles (estado)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_recaudo_detalles_updated_at
BEFORE UPDATE ON recaudo_detalles
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 13. PAGOS
-- ============================================================

CREATE TABLE pagos (
    id BIGSERIAL PRIMARY KEY,

    prestamo_id BIGINT NOT NULL,
    cuota_prestamo_id BIGINT,
    recaudo_detalle_id BIGINT,

    cliente_id BIGINT NOT NULL,
    ruta_id BIGINT NOT NULL,
    trabajador_id BIGINT NOT NULL,

    fecha_pago TIMESTAMP NOT NULL DEFAULT NOW(),

    valor_pago NUMERIC(18,2) NOT NULL,
    forma_pago VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO',

    estado VARCHAR(30) NOT NULL DEFAULT 'APLICADO',
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_pagos_prestamos
        FOREIGN KEY (prestamo_id) REFERENCES prestamos(id),

    CONSTRAINT fk_pagos_cuotas_prestamo
        FOREIGN KEY (cuota_prestamo_id) REFERENCES cuotas_prestamo(id),

    CONSTRAINT fk_pagos_recaudo_detalles
        FOREIGN KEY (recaudo_detalle_id) REFERENCES recaudo_detalles(id),

    CONSTRAINT fk_pagos_clientes
        FOREIGN KEY (cliente_id) REFERENCES clientes(id),

    CONSTRAINT fk_pagos_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT fk_pagos_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT chk_pagos_valor
        CHECK (valor_pago > 0),

    CONSTRAINT chk_pagos_forma_pago
        CHECK (forma_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'NEQUI', 'DAVIPLATA', 'OTRO')),

    CONSTRAINT chk_pagos_estado
        CHECK (estado IN ('APLICADO', 'ANULADO', 'REVERSADO'))
);

CREATE INDEX idx_pagos_fecha
ON pagos (fecha_pago)
WHERE deleted_at IS NULL;

CREATE INDEX idx_pagos_cliente
ON pagos (cliente_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_pagos_trabajador
ON pagos (trabajador_id)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_pagos_updated_at
BEFORE UPDATE ON pagos
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 14. CAJAS_DIARIAS
-- Controla el dinero que maneja el trabajador.
-- ============================================================

CREATE TABLE cajas_diarias (
    id BIGSERIAL PRIMARY KEY,

    trabajador_id BIGINT NOT NULL,
    ruta_id BIGINT NOT NULL,

    fecha_caja DATE NOT NULL,

    valor_inicial NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_prestamos_entregados NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_recaudado NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_esperado_cierre NUMERIC(18,2) NOT NULL DEFAULT 0,
    valor_entregado NUMERIC(18,2) NOT NULL DEFAULT 0,
    diferencia NUMERIC(18,2) NOT NULL DEFAULT 0,

    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTA',
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_cajas_diarias_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT fk_cajas_diarias_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT chk_cajas_diarias_valores
        CHECK (
            valor_inicial >= 0
            AND valor_prestamos_entregados >= 0
            AND valor_recaudado >= 0
            AND valor_esperado_cierre >= 0
            AND valor_entregado >= 0
        ),

    CONSTRAINT chk_cajas_diarias_estado
        CHECK (estado IN ('ABIERTA', 'CERRADA', 'CERRADA_CON_DIFERENCIA', 'ANULADA'))
);

CREATE UNIQUE INDEX uq_cajas_diarias_trabajador_fecha
ON cajas_diarias (trabajador_id, fecha_caja)
WHERE deleted_at IS NULL AND estado <> 'ANULADA';

CREATE INDEX idx_cajas_diarias_ruta_fecha
ON cajas_diarias (ruta_id, fecha_caja)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_cajas_diarias_updated_at
BEFORE UPDATE ON cajas_diarias
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 15. MOVIMIENTOS_CAJA
-- Cada préstamo entregado y cada pago recibido debe generar movimiento.
-- ============================================================

CREATE TABLE movimientos_caja (
    id BIGSERIAL PRIMARY KEY,

    caja_diaria_id BIGINT NOT NULL,
    trabajador_id BIGINT NOT NULL,
    ruta_id BIGINT NOT NULL,

    tipo_movimiento VARCHAR(50) NOT NULL,
    valor NUMERIC(18,2) NOT NULL,

    referencia_tipo VARCHAR(50),
    referencia_id BIGINT,

    fecha_movimiento TIMESTAMP NOT NULL DEFAULT NOW(),
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_movimientos_caja_cajas_diarias
        FOREIGN KEY (caja_diaria_id) REFERENCES cajas_diarias(id),

    CONSTRAINT fk_movimientos_caja_trabajadores
        FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),

    CONSTRAINT fk_movimientos_caja_rutas
        FOREIGN KEY (ruta_id) REFERENCES rutas(id),

    CONSTRAINT chk_movimientos_caja_valor
        CHECK (valor > 0),

    CONSTRAINT chk_movimientos_caja_tipo
        CHECK (
            tipo_movimiento IN (
                'CAJA_INICIAL',
                'PRESTAMO_ENTREGADO',
                'PAGO_RECIBIDO',
                'AJUSTE',
                'CIERRE_CAJA',
                'ANULACION_PAGO',
                'ANULACION_PRESTAMO'
            )
        )
);

CREATE INDEX idx_movimientos_caja_caja
ON movimientos_caja (caja_diaria_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_movimientos_caja_tipo
ON movimientos_caja (tipo_movimiento)
WHERE deleted_at IS NULL;

CREATE INDEX idx_movimientos_caja_fecha
ON movimientos_caja (fecha_movimiento)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_movimientos_caja_updated_at
BEFORE UPDATE ON movimientos_caja
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 15.A. PRESUPUESTO_ADMIN
-- Presupuesto / capital del administrador. Representa el monto inicial
-- desde el cual parte toda la operación de préstamos. Un único registro
-- ACTIVO a la vez; el saldo disponible se calcula a partir de los
-- movimientos + los préstamos vigentes + los pagos recibidos.
-- ============================================================

CREATE TABLE presupuesto_admin (
    id BIGSERIAL PRIMARY KEY,

    monto_inicial NUMERIC(18, 2) NOT NULL DEFAULT 0,
    fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE,
    observacion TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT chk_presupuesto_admin_estado
        CHECK (estado IN ('ACTIVO', 'CERRADO')),

    CONSTRAINT chk_presupuesto_admin_monto
        CHECK (monto_inicial >= 0)
);

CREATE UNIQUE INDEX uq_presupuesto_admin_activo
ON presupuesto_admin (estado)
WHERE estado = 'ACTIVO' AND deleted_at IS NULL;

CREATE TRIGGER trg_presupuesto_admin_updated_at
BEFORE UPDATE ON presupuesto_admin
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 15.B. MOVIMIENTOS_PRESUPUESTO
-- Historial de capitalizaciones / retiros / apertura del presupuesto.
-- Cualquier ajuste al capital del administrador deja huella aquí.
-- ============================================================

CREATE TABLE movimientos_presupuesto (
    id BIGSERIAL PRIMARY KEY,

    presupuesto_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    valor NUMERIC(18, 2) NOT NULL,
    observacion TEXT,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT NOW(),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_movimientos_presupuesto_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto_admin(id),

    CONSTRAINT chk_movimientos_presupuesto_tipo
        CHECK (tipo IN ('APERTURA', 'CAPITALIZACION', 'RETIRO', 'AJUSTE')),

    CONSTRAINT chk_movimientos_presupuesto_valor
        CHECK (valor > 0)
);

CREATE INDEX idx_movimientos_presupuesto_presupuesto
ON movimientos_presupuesto (presupuesto_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_movimientos_presupuesto_fecha
ON movimientos_presupuesto (fecha_movimiento)
WHERE deleted_at IS NULL;

CREATE TRIGGER trg_movimientos_presupuesto_updated_at
BEFORE UPDATE ON movimientos_presupuesto
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- 16. AUDITORIA_GENERAL
-- Para acciones sensibles.
-- ============================================================

CREATE TABLE auditoria_general (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,
    tabla VARCHAR(100) NOT NULL,
    registro_id BIGINT NOT NULL,
    accion VARCHAR(50) NOT NULL,

    valor_anterior JSONB,
    valor_nuevo JSONB,

    fecha_accion TIMESTAMP NOT NULL DEFAULT NOW(),
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_auditoria_general_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_auditoria_general_tabla_registro
ON auditoria_general (tabla, registro_id);

CREATE INDEX idx_auditoria_general_fecha
ON auditoria_general (fecha_accion);

CREATE INDEX idx_auditoria_general_accion
ON auditoria_general (accion);

CREATE TRIGGER trg_auditoria_general_updated_at
BEFORE UPDATE ON auditoria_general
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- FUNCIÓN: INSERTAR CLIENTE EN UNA POSICIÓN DE LA RUTA
--
-- Uso:
-- SELECT fn_insertar_cliente_en_ruta(
--     p_ruta_id := 1,
--     p_cliente_id := 10,
--     p_orden_base := 5,
--     p_usuario_id := 1
-- );
--
-- Resultado:
-- Inserta el cliente después del orden 5, quedando en orden 6.
-- Los clientes posteriores se corren automáticamente.
--
-- Si p_orden_base es NULL, se inserta al final de la ruta.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_insertar_cliente_en_ruta(
    p_ruta_id BIGINT,
    p_cliente_id BIGINT,
    p_orden_base INTEGER,
    p_usuario_id BIGINT
)
RETURNS BIGINT AS $$
DECLARE
    v_nuevo_orden INTEGER;
    v_ruta_cliente_id BIGINT;
BEGIN
    IF p_ruta_id IS NULL THEN
        RAISE EXCEPTION 'La ruta es obligatoria';
    END IF;

    IF p_cliente_id IS NULL THEN
        RAISE EXCEPTION 'El cliente es obligatorio';
    END IF;

    IF p_orden_base IS NULL THEN
        SELECT COALESCE(MAX(orden), 0) + 1
        INTO v_nuevo_orden
        FROM ruta_clientes
        WHERE ruta_id = p_ruta_id
          AND deleted_at IS NULL
          AND activo = TRUE;
    ELSE
        v_nuevo_orden := p_orden_base + 1;

        -- Para evitar conflictos con el índice único, primero movemos temporalmente.
        UPDATE ruta_clientes
        SET orden = orden + 10000,
            updated_by = p_usuario_id
        WHERE ruta_id = p_ruta_id
          AND orden > p_orden_base
          AND deleted_at IS NULL
          AND activo = TRUE;

        -- Luego dejamos los órdenes definitivos corridos en una posición.
        UPDATE ruta_clientes
        SET orden = orden - 9999,
            updated_by = p_usuario_id
        WHERE ruta_id = p_ruta_id
          AND orden > (p_orden_base + 10000)
          AND deleted_at IS NULL
          AND activo = TRUE;
    END IF;

    INSERT INTO ruta_clientes (
        ruta_id,
        cliente_id,
        orden,
        updated_by
    ) VALUES (
        p_ruta_id,
        p_cliente_id,
        v_nuevo_orden,
        p_usuario_id
    )
    RETURNING id INTO v_ruta_cliente_id;

    INSERT INTO auditoria_general (
        usuario_id,
        tabla,
        registro_id,
        accion,
        valor_nuevo,
        observacion,
        updated_by
    ) VALUES (
        p_usuario_id,
        'ruta_clientes',
        v_ruta_cliente_id,
        'INSERTAR_CLIENTE_EN_RUTA',
        jsonb_build_object(
            'ruta_id', p_ruta_id,
            'cliente_id', p_cliente_id,
            'orden', v_nuevo_orden
        ),
        'Cliente insertado en la ruta conservando el orden operativo.',
        p_usuario_id
    );

    RETURN v_ruta_cliente_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- DATOS INICIALES
-- ============================================================

INSERT INTO roles (nombre, descripcion)
VALUES
('ADMINISTRADOR', 'Usuario administrador del sistema'),
('TRABAJADOR', 'Usuario cobrador de ruta')
ON CONFLICT DO NOTHING;

-- Usuario administrador inicial.
-- Cambiar el password_hash desde el backend usando BCrypt.
INSERT INTO usuarios (
    rol_id,
    nombre,
    usuario,
    email,
    password_hash,
    activo
)
SELECT
    r.id,
    'Administrador Principal',
    'admin',
    'admin@recaudo.local',
    'CAMBIAR_PASSWORD_HASH',
    TRUE
FROM roles r
WHERE r.nombre = 'ADMINISTRADOR'
ON CONFLICT DO NOTHING;

-- ============================================================
-- CONSULTAS ÚTILES DE VALIDACIÓN
-- ============================================================

/*
-- Ver rutas con clientes en orden:
SELECT
    r.nombre AS ruta,
    rc.orden,
    c.nombre AS cliente,
    c.telefono,
    c.direccion
FROM ruta_clientes rc
INNER JOIN rutas r ON r.id = rc.ruta_id
INNER JOIN clientes c ON c.id = rc.cliente_id
WHERE rc.deleted_at IS NULL
  AND rc.activo = TRUE
ORDER BY r.nombre, rc.orden;

-- Ver préstamos activos por cliente:
SELECT
    c.nombre AS cliente,
    p.monto_prestado,
    p.tasa_porcentaje,
    p.total_pagar,
    p.cuota_diaria,
    p.saldo_actual,
    p.estado
FROM prestamos p
INNER JOIN clientes c ON c.id = p.cliente_id
WHERE p.deleted_at IS NULL
  AND p.estado = 'ACTIVO'
ORDER BY c.nombre;

-- Ver caja diaria del trabajador:
SELECT
    t.nombre AS trabajador,
    r.nombre AS ruta,
    cd.fecha_caja,
    cd.valor_inicial,
    cd.valor_prestamos_entregados,
    cd.valor_recaudado,
    cd.valor_esperado_cierre,
    cd.valor_entregado,
    cd.diferencia,
    cd.estado
FROM cajas_diarias cd
INNER JOIN trabajadores t ON t.id = cd.trabajador_id
INNER JOIN rutas r ON r.id = cd.ruta_id
WHERE cd.deleted_at IS NULL
ORDER BY cd.fecha_caja DESC;
*/

COMMIT;
