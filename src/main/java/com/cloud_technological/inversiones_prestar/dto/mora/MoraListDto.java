package com.cloud_technological.inversiones_prestar.dto.mora;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del reporte de clientes en mora (HU-BE-019 / HU-FE-021).
 * Los nombres de campo coinciden con los alias de la consulta SQL (snake_case).
 */
@Getter
@Setter
@NoArgsConstructor
public class MoraListDto {

    private Long cliente_id;
    private String cliente;
    private String documento;
    private String telefono;

    private Long ruta_id;
    private String ruta;

    private Long trabajador_id;
    private String trabajador;

    private Long prestamo_id;

    /** Fecha de la cuota vencida más antigua aún con saldo. */
    private LocalDate cuota_vencida_mas_antigua;

    /** Días de mora = hoy - fecha de la cuota vencida más antigua. */
    private Integer dias_mora;

    /** Cantidad de cuotas vencidas con saldo pendiente. */
    private Integer cuotas_vencidas;

    /** Suma del saldo de las cuotas vencidas. */
    private BigDecimal saldo_vencido;

    /** Saldo total pendiente del préstamo. */
    private BigDecimal saldo_pendiente;
}
