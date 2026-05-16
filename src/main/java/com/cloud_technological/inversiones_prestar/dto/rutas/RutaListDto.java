package com.cloud_technological.inversiones_prestar.dto.rutas;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de rutas.
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class RutaListDto {

    private Long id;
    private String nombre;
    private String zona;
    private String descripcion;
    private String estado;
    private String trabajador;
    private Long trabajador_id;
    private Integer clientes;
    private LocalDate fecha_creacion;
}
