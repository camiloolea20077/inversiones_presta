package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReordenarRutaDto {

    /** IDs de ruta_clientes en el nuevo orden del recorrido. */
    @NotEmpty(message = "El orden no puede estar vacío")
    private List<Long> ordenIds;
}
