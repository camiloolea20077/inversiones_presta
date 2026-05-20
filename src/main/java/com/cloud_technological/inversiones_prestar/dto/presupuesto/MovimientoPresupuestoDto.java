package com.cloud_technological.inversiones_prestar.dto.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoPresupuestoDto {

    private Long id;
    private String tipo;
    private BigDecimal valor;
    private String observacion;
    private LocalDateTime fechaMovimiento;
}
