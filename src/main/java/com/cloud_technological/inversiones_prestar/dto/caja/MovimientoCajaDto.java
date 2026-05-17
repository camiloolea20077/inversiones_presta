package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Renglón de un movimiento de caja (HU-BE-016). */
@Getter
@Setter
@Builder
public class MovimientoCajaDto {

    private Long id;
    private String tipoMovimiento;
    private BigDecimal valor;
    private String referenciaTipo;
    private Long referenciaId;
    private LocalDateTime fechaMovimiento;
    private String observacion;
}
