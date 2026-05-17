package com.cloud_technological.inversiones_prestar.dto.auditoria;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de auditoría (HU-BE-020 / HU-FE-022).
 * Los nombres de campo coinciden con los alias de la consulta SQL (snake_case).
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaListDto {

    private Long id;

    /** Fecha y hora del evento. */
    private LocalDateTime fecha;

    private Long usuario_id;
    private String usuario;

    /** Acción ejecutada (CREAR, PAGAR, ANULAR, REORDENAR, CERRAR_CAJA, ...). */
    private String accion;

    /** Tabla / entidad afectada. */
    private String tabla_afectada;

    /** Id del registro afectado. */
    private Long registro_id;

    /** Descripción legible del evento. */
    private String observacion;

    /** Estado anterior del registro como JSON (texto). Puede ser null. */
    private String valor_anterior;

    /** Estado nuevo del registro como JSON (texto). Puede ser null. */
    private String valor_nuevo;
}
