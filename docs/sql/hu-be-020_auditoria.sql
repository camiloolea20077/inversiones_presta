/*
============================================================
 HU-BE-020 — Auditoría general del sistema.

 Tabla eventos_auditoria: registra acciones sensibles
 (creación de clientes, creación de préstamos, pagos,
 anulaciones, cambios de orden en ruta, cierres de caja),
 guardando usuario, acción, tabla y registro afectado, y
 los valores anterior/nuevo serializados como JSON (texto).

 Script IDEMPOTENTE: crea la tabla, los índices y el trigger
 solo si faltan. El backend NO genera DDL; ejecutar a mano.
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
-- EVENTOS_AUDITORIA
-- ============================================================

CREATE TABLE IF NOT EXISTS eventos_auditoria (
    id BIGSERIAL PRIMARY KEY,

    -- Usuario que ejecutó la acción (puede ser NULL en procesos del sistema).
    usuario_id BIGINT,
    -- Nombre del usuario cacheado al momento del evento (para no depender del join).
    usuario_nombre VARCHAR(150),

    -- Acción ejecutada: CREAR, ACTUALIZAR, PAGAR, ANULAR, REORDENAR, CERRAR_CAJA, etc.
    accion VARCHAR(50) NOT NULL,

    -- Tabla / entidad afectada (clientes, prestamos, pagos, ruta_clientes, cajas_diarias...).
    tabla_afectada VARCHAR(80) NOT NULL,

    -- Id del registro afectado en esa tabla.
    registro_id BIGINT,

    -- Estado anterior y nuevo del registro, serializados como JSON (texto).
    valor_anterior TEXT,
    valor_nuevo TEXT,

    -- Descripción legible del evento.
    observacion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    updated_by BIGINT,

    CONSTRAINT fk_eventos_auditoria_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX IF NOT EXISTS idx_eventos_auditoria_fecha
ON eventos_auditoria (created_at)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_eventos_auditoria_usuario
ON eventos_auditoria (usuario_id)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_eventos_auditoria_accion
ON eventos_auditoria (accion)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_eventos_auditoria_tabla
ON eventos_auditoria (tabla_afectada)
WHERE deleted_at IS NULL;

DROP TRIGGER IF EXISTS trg_eventos_auditoria_updated_at ON eventos_auditoria;
CREATE TRIGGER trg_eventos_auditoria_updated_at
BEFORE UPDATE ON eventos_auditoria
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

COMMIT;

/*
-- Validación: últimos eventos de auditoría.
SELECT
    ea.created_at,
    ea.usuario_nombre,
    ea.accion,
    ea.tabla_afectada,
    ea.registro_id,
    ea.observacion
FROM eventos_auditoria ea
WHERE ea.deleted_at IS NULL
ORDER BY ea.created_at DESC
LIMIT 50;
*/
