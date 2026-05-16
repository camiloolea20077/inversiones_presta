package com.cloud_technological.inversiones_prestar.dto.pagos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarcarNoPagoDto {

    @NotNull(message = "El renglón del recaudo es obligatorio")
    private Long recaudoDetalleId;

    private String observacion;
}
