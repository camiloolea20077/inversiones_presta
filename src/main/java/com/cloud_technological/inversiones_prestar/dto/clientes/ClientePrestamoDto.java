package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Préstamo activo del cliente, resumido para el panel de detalle de la
 * pantalla "Clientes y Préstamos". Los nombres coinciden con los alias SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientePrestamoDto {

    private Long id;
    private BigDecimal monto_prestado;
    private BigDecimal tasa_porcentaje;
    private String tipo_interes;
    private BigDecimal valor_interes;
    private BigDecimal total_pagar;
    private BigDecimal cuota_diaria;
    private BigDecimal saldo_actual;
    private Integer plazo_dias;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private String estado;

    /** Total de cuotas del préstamo. */
    private Integer cuotas_total;
    /** Cuotas en estado PAGADA. */
    private Integer cuotas_pagadas;
    /** Días transcurridos desde la fecha de inicio. */
    private Integer dias_transcurridos;
    /** Días de mora (0 si está al día). */
    private Integer dias_mora;
}
