package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LimiteTrabajadorResponseDto {

    private Long id;
    private Long trabajadorId;
    private BigDecimal montoMaximoPrestamo;
    private BigDecimal tasaMinima;
    private BigDecimal tasaMaxima;
    private Integer plazoMaximoDias;
    private Boolean puedeCrearCliente;
    private Boolean puedeCrearPrestamo;
    private Boolean puedeDefinirTasa;
    private Boolean activo;
}
