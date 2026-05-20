package com.cloud_technological.inversiones_prestar.dto.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del presupuesto del administrador. Trae los totales necesarios
 * para validar préstamos y para mostrar el panel de capital.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoResponseDto {

    private Long id;
    private BigDecimal montoInicial;
    private LocalDate fechaApertura;
    private String observacion;
    private String estado;

    /** Aperturas + capitalizaciones - retiros. */
    private BigDecimal capitalAportado;

    /** Capital aportado - total prestado + total recaudado. */
    private BigDecimal saldoDisponible;

    /** Total prestado histórico (capital). */
    private BigDecimal totalPrestado;

    /** Total recaudado histórico (todos los pagos). */
    private BigDecimal totalRecaudado;

    /** Capital aún en la calle (no recuperado). */
    private BigDecimal capitalEnCalle;

    private List<MovimientoPresupuestoDto> movimientos;
}
