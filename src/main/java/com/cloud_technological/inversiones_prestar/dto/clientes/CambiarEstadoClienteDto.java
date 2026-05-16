package com.cloud_technological.inversiones_prestar.dto.clientes;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarEstadoClienteDto {

    /** ACTIVO, BLOQUEADO, RETIRADO. */
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
