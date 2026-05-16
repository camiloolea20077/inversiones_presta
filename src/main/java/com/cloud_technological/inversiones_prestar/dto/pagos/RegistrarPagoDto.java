package com.cloud_technological.inversiones_prestar.dto.pagos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarPagoDto {

    @NotNull(message = "El renglón del recaudo es obligatorio")
    private Long recaudoDetalleId;

    @NotNull(message = "El valor del pago es obligatorio")
    @DecimalMin(value = "1", message = "El valor del pago debe ser mayor a cero")
    private BigDecimal valorPago;

    /** EFECTIVO, TRANSFERENCIA, NEQUI, DAVIPLATA, OTRO. Por defecto EFECTIVO. */
    private String formaPago;

    private String observacion;
}
