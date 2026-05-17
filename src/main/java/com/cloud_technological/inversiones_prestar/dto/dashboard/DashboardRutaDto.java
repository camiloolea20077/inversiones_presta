package com.cloud_technological.inversiones_prestar.dto.dashboard;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fila del resumen por ruta del dashboard administrativo.
 * Los nombres de campo coinciden con los alias de la consulta SQL (snake_case).
 */
@Getter
@Setter
@NoArgsConstructor
public class DashboardRutaDto {

    private Long ruta_id;
    private String ruta;
    private String zona;
    private String trabajador;
    private Long clientes;
    private BigDecimal total_esperado;
    private BigDecimal total_recaudado;
    private Integer progreso;
    private String estado;
}
