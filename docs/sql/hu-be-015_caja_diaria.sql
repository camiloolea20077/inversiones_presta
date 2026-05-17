/*
============================================================
 HU-BE-015 / HU-BE-016 — Caja diaria del trabajador y
 movimientos de caja.

 Las tablas cajas_diarias y movimientos_caja YA existen en
 el esquema base (docs/agents/recaudo_diario_schema.sql).
 Este script es IDEMPOTENTE: crea las tablas, índices y
 triggers solo si faltan, para poder aplicarlo sobre una
 base de datos ya en uso sin recrear el esquema completo.

 El backend NO genera DDL; ejecutar este script manualmente.
============================================================
*/

BEGIN;

-- Función de updated_at (puede existir ya).
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- CAJAS_DIARIAS
-- ============================================================

CREATE TABLE IF NOT EXISTS cajas_diarias (
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

-- Una sola caja vigente por trabajador y fecha (evita doble apertura).
CREATE UNIQUE INDEX IF NOT EXISTS uq_cajas_diarias_trabajador_fecha
ON cajas_diarias (trabajador_id, fecha_caja)
WHERE deleted_at IS NULL AND estado <> 'ANULADA';

CREATE INDEX IF NOT EXISTS idx_cajas_diarias_ruta_fecha
ON cajas_diarias (ruta_id, fecha_caja)
WHERE deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_cajas_diarias_updated_at ON cajas_diarias;
CREATE TRIGGER trg_cajas_diarias_updated_at
BEFORE UPDATE ON cajas_diarias
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- MOVIMIENTOS_CAJA
-- ============================================================

CREATE TABLE IF NOT EXISTS movimientos_caja (
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

CREATE INDEX IF NOT EXISTS idx_movimientos_caja_caja
ON movimientos_caja (caja_diaria_id)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_movimientos_caja_tipo
ON movimientos_caja (tipo_movimiento)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_movimientos_caja_fecha
ON movimientos_caja (fecha_movimiento)
WHERE deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_movimientos_caja_updated_at ON movimientos_caja;
CREATE TRIGGER trg_movimientos_caja_updated_at
BEFORE UPDATE ON movimientos_caja
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

COMMIT;

/*
-- Validación: estado de la caja diaria del trabajador.
SELECT
    t.nombre AS trabajador,
    r.nombre AS ruta,
    cd.fecha_caja,
    cd.valor_inicial,
    cd.valor_prestamos_entregados,
    cd.valor_recaudado,
    cd.valor_esperado_cierre,   -- = valor_inicial - prestamos + recaudado
    cd.valor_entregado,
    cd.diferencia,              -- = valor_entregado - valor_esperado_cierre
    cd.estado
FROM cajas_diarias cd
INNER JOIN trabajadores t ON t.id = cd.trabajador_id
INNER JOIN rutas r ON r.id = cd.ruta_id
WHERE cd.deleted_at IS NULL
ORDER BY cd.fecha_caja DESC;

-- Movimientos de una caja:
SELECT tipo_movimiento, valor, referencia_tipo, referencia_id, fecha_movimiento
FROM movimientos_caja
WHERE caja_diaria_id = :cajaId AND deleted_at IS NULL
ORDER BY fecha_movimiento;
*/
