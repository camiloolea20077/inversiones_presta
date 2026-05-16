package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimulacionResponseDto {

    private BigDecimal montoPrestado;
    private BigDecimal valorInteres;
    private BigDecimal totalPagar;
    private BigDecimal cuotaDiaria;
    private Integer plazoDias;
}
