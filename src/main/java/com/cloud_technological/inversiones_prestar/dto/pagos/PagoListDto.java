package com.cloud_technological.inversiones_prestar.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de pagos (HU-FE-019).
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class PagoListDto {

    private Long id;
    private LocalDateTime fecha_pago;
    private Long cliente_id;
    private String cliente;
    private Long trabajador_id;
    private String trabajador;
    private Long ruta_id;
    private String ruta;
    private BigDecimal valor_pago;
    private String forma_pago;
    private String estado;
    private Long prestamo_id;
}
