package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de cajas diarias de los trabajadores
 * (HU-FE-020). Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class CajaListDto {

    private Long id;
    private Long trabajador_id;
    private String trabajador;
    private Long ruta_id;
    private String ruta;
    private LocalDate fecha_caja;
    private BigDecimal valor_inicial;
    private BigDecimal valor_prestamos_entregados;
    private BigDecimal valor_recaudado;
    private BigDecimal valor_esperado_cierre;
    private BigDecimal valor_entregado;
    private BigDecimal diferencia;
    private String estado;
    private String observacion;
}
