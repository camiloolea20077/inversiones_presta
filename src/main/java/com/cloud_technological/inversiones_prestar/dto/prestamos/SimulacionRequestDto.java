package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulacionRequestDto {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1", message = "El monto debe ser mayor a cero")
    private BigDecimal montoPrestado;

    @NotNull(message = "La tasa es obligatoria")
    @DecimalMin(value = "0", message = "La tasa no puede ser negativa")
    private BigDecimal tasaPorcentaje;

    private String tipoInteres;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 día")
    private Integer plazoDias;
}
