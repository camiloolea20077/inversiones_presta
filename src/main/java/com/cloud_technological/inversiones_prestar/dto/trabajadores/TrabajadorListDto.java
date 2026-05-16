package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de una fila del listado de trabajadores.
 * Los nombres de campo coinciden con los alias de la consulta SQL.
 */
@Getter
@Setter
@NoArgsConstructor
public class TrabajadorListDto {

    private Long id;
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private String estado;
    private String usuario_nombre;
    private String usuario_correo;
    private String rol;
    private String tiene_limite;
    private String ruta_asignada;
    private Integer clientes;
    private BigDecimal recaudo_hoy;
    private LocalDate fecha_ingreso;
}
