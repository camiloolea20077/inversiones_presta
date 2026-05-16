package com.cloud_technological.inversiones_prestar.dto.clientes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cliente dentro del recorrido de una ruta, en orden.
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class RutaClienteListDto {

    private Long id;
    private Long cliente_id;
    private Integer orden;
    private String nombre;
    private String documento;
    private String telefono;
    private String direccion;
    private String barrio;
}
