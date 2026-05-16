package com.cloud_technological.inversiones_prestar.dto.clientes;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsertarClienteRutaDto {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    /**
     * Orden del cliente después del cual se inserta el nuevo.
     * Si es null, el cliente se agrega al final del recorrido.
     */
    private Integer ordenBase;
}
