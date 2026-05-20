package com.cloud_technological.inversiones_prestar.dto.presupuesto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class AbrirPresupuestoRequestDto {

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto inicial debe ser mayor a 0")
    private BigDecimal montoInicial;

    private String observacion;
}
