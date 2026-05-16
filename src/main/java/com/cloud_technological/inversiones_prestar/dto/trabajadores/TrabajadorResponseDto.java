package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrabajadorResponseDto {

    private Long id;
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private Long usuarioId;
    private String usuarioNombre;
    private String correo;
    private String usuario;
}
