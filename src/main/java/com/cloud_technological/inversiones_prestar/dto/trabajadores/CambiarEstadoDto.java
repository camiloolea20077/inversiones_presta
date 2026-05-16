package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarEstadoDto {

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
