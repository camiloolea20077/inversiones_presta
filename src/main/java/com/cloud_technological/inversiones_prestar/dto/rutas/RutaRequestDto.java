package com.cloud_technological.inversiones_prestar.dto.rutas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RutaRequestDto {

    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 150, message = "La zona no puede superar 150 caracteres")
    private String zona;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    private Boolean activo;

    /** Trabajador a asignar a la ruta (opcional; null = sin asignar). */
    private Long trabajadorId;
}
