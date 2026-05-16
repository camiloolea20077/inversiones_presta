package com.cloud_technological.inversiones_prestar.dto.clientes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequestDto {

    @Size(max = 30, message = "El documento no puede superar 30 caracteres")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccion;

    @Size(max = 150, message = "El barrio no puede superar 150 caracteres")
    private String barrio;

    private String observacion;

    /** ACTIVO, BLOQUEADO, RETIRADO. Opcional (por defecto ACTIVO). */
    private String estado;
}
