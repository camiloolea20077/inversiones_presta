package com.cloud_technological.inversiones_prestar.dto.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reporte de rentabilidad del administrador a partir del presupuesto inicial.
 * Combina la información del capital con los préstamos y pagos para mostrar
 * cuánto se ha ganado y cuánto se proyecta ganar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentabilidadDto {

    /** Fecha en que se calculó el reporte. */
    private LocalDate fechaCorte;

    // === Capital ===
    /** Capital aportado por el administrador (aperturas + capitalizaciones - retiros). */
    private BigDecimal capitalAportado;

    /** Saldo en caja (lo que aún no se ha prestado). */
    private BigDecimal saldoDisponible;

    /** Total de capital prestado históricamente. */
    private BigDecimal capitalPrestadoHistorico;

    /** Capital aún en la calle (no recuperado). */
    private BigDecimal capitalEnCalle;

    /** Capital ya recuperado. */
    private BigDecimal capitalRecuperado;

    // === Cartera ===
    /** Saldo total por cobrar de los préstamos activos. */
    private BigDecimal carteraActiva;

    private Long prestamosActivos;
    private Long prestamosPagados;

    // === Intereses ===
    /** Intereses totales proyectados de la operación completa. */
    private BigDecimal interesesProyectadosTotales;

    /** Intereses ya cobrados proporcionalmente. */
    private BigDecimal interesesCobrados;

    /** Intereses pendientes de cobrar (proyectados - cobrados). */
    private BigDecimal interesesPorCobrar;

    // === Rentabilidad ===
    /** % de rentabilidad realizada = intereses cobrados / capital aportado. */
    private BigDecimal rentabilidadRealizadaPorcentaje;

    /** % de rentabilidad proyectada = intereses proyectados / capital aportado. */
    private BigDecimal rentabilidadProyectadaPorcentaje;

    // === Mora ===
    /** Saldo en mora (cuotas vencidas no pagadas). */
    private BigDecimal totalEnMora;

    /** Cantidad de clientes con cuotas vencidas. */
    private Long clientesEnMora;

    /** % de mora sobre la cartera activa. */
    private BigDecimal porcentajeMora;
}
