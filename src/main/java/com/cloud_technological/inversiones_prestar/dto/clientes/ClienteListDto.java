package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de clientes.
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClienteListDto {

    private Long id;
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private String barrio;
    private String estado;
    private String ruta;
    private Long ruta_id;
    private LocalDate fecha_registro;
}
