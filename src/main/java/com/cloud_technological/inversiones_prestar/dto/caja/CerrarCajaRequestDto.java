package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Solicitud para cerrar una caja diaria registrando el valor entregado. */
@Getter
@Setter
public class CerrarCajaRequestDto {

    @NotNull(message = "La caja es obligatoria")
    private Long cajaId;

    @NotNull(message = "El valor entregado es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor entregado no puede ser negativo")
    private BigDecimal valorEntregado;

    private String observacion;
}
