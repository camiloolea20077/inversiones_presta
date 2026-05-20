/*
============================================================
 MIGRACIÓN: PRESUPUESTO DEL ADMINISTRADOR
 Aplica este script sobre una BD ya inicializada con
 recaudo_diario_schema.sql para añadir las tablas de
 presupuesto / capital del administrador.
============================================================
*/

BEGIN;

CREATE TABLE IF NOT EXISTS presupuesto_admin (
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

CREATE UNIQUE INDEX IF NOT EXISTS uq_presupuesto_admin_activo
ON presupuesto_admin (estado)
WHERE estado = 'ACTIVO' AND deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_presupuesto_admin_updated_at ON presupuesto_admin;
CREATE TRIGGER trg_presupuesto_admin_updated_at
BEFORE UPDATE ON presupuesto_admin
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

CREATE TABLE IF NOT EXISTS movimientos_presupuesto (
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

CREATE INDEX IF NOT EXISTS idx_movimientos_presupuesto_presupuesto
ON movimientos_presupuesto (presupuesto_id)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_movimientos_presupuesto_fecha
ON movimientos_presupuesto (fecha_movimiento)
WHERE deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_movimientos_presupuesto_updated_at ON movimientos_presupuesto;
CREATE TRIGGER trg_movimientos_presupuesto_updated_at
BEFORE UPDATE ON movimientos_presupuesto
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

COMMIT;
