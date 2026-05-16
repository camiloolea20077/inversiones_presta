package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de préstamos.
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class PrestamoListDto {

    private Long id;
    private String cliente;
    private Long cliente_id;
    private String ruta;
    private String trabajador;
    private BigDecimal monto_prestado;
    private BigDecimal total_pagar;
    private BigDecimal saldo_actual;
    private BigDecimal cuota_diaria;
    private Integer plazo_dias;
    private String estado;
    private LocalDate fecha_inicio;
}
