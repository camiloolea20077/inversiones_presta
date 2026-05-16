package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimiteTrabajadorRequestDto {

    @NotNull(message = "El monto máximo por préstamo es obligatorio")
    @PositiveOrZero(message = "El monto máximo no puede ser negativo")
    private BigDecimal montoMaximoPrestamo;

    @NotNull(message = "La tasa mínima es obligatoria")
    @PositiveOrZero(message = "La tasa mínima no puede ser negativa")
    private BigDecimal tasaMinima;

    @NotNull(message = "La tasa máxima es obligatoria")
    @PositiveOrZero(message = "La tasa máxima no puede ser negativa")
    private BigDecimal tasaMaxima;

    @NotNull(message = "El plazo máximo en días es obligatorio")
    @PositiveOrZero(message = "El plazo máximo no puede ser negativo")
    private Integer plazoMaximoDias;

    private Boolean puedeCrearCliente;
    private Boolean puedeCrearPrestamo;
    private Boolean puedeDefinirTasa;
}
