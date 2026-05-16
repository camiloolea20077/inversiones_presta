package com.cloud_technological.inversiones_prestar.dto.rutas;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RutaResponseDto {

    private Long id;
    private String nombre;
    private String zona;
    private String descripcion;
    private Boolean activo;
    private Long trabajadorId;
    private String trabajadorNombre;
    private LocalDate fechaAsignacion;
    private Integer clientes;
}
