package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClienteResponseDto {

    private Long id;
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private String barrio;
    private String observacion;
    private String estado;
    private Boolean activo;
    private LocalDate fechaRegistro;
    private String ruta;
}
