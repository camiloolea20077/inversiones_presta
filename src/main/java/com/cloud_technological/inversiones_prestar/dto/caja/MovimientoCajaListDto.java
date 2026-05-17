package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de movimientos de caja (HU-BE-016).
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class MovimientoCajaListDto {

    private Long id;
    private Long caja_diaria_id;
    private Long trabajador_id;
    private String trabajador;
    private Long ruta_id;
    private String ruta;
    private LocalDate fecha_caja;
    private String tipo_movimiento;
    private BigDecimal valor;
    private String referencia_tipo;
    private Long referencia_id;
    private LocalDateTime fecha_movimiento;
    private String observacion;
}
