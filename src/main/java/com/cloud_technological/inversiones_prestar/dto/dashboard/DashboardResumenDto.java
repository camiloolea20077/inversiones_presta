package com.cloud_technological.inversiones_prestar.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Indicadores generales del negocio para el dashboard administrativo (HU-BE-018).
 */
@Getter
@Setter
@Builder
public class DashboardResumenDto {

    /** Suma del monto prestado de todos los préstamos activos. */
    private BigDecimal totalPrestado;

    /** Total recaudado en la fecha consultada. */
    private BigDecimal totalRecaudadoHoy;

    /** Cartera activa = suma de saldos de préstamos activos. */
    private BigDecimal carteraActiva;

    /** Cantidad de clientes con cuotas vencidas (en mora). */
    private Long clientesEnMora;

    /** Cantidad de rutas activas. */
    private Long rutasActivas;

    /** Cantidad de trabajadores activos. */
    private Long trabajadoresActivos;

    /** Recaudo esperado del día (suma de total_esperado de las planillas). */
    private BigDecimal recaudoEsperado;

    /** Recaudo real del día (suma de total_recaudado de las planillas). */
    private BigDecimal recaudoReal;

    /** Porcentaje de cumplimiento del recaudo (real / esperado * 100). */
    private BigDecimal cumplimientoPorcentaje;

    /** Diferencia entre recaudo esperado y real. */
    private BigDecimal diferenciaRecaudo;

    /** Resumen del recaudo del día por cada ruta. */
    private List<DashboardRutaDto> resumenRutas;
}
