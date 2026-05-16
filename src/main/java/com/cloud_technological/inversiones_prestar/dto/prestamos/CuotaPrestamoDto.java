package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CuotaPrestamoDto {

    private Long id;
    private Integer numeroCuota;
    private LocalDate fechaCuota;
    private BigDecimal valorCuota;
    private BigDecimal valorPagado;
    private BigDecimal saldoCuota;
    private String estado;
}
