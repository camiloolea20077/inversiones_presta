package com.cloud_technological.inversiones_prestar.dto.rutas;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarTrabajadorDto {

    @NotNull(message = "El trabajador es obligatorio")
    private Long trabajadorId;
}
