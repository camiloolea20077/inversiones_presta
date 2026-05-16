package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Trabajador en formato reducido para selects/combos. */
@Getter
@Setter
@Builder
public class TrabajadorComboDto {

    private Long id;
    private String nombre;
    private String documento;
}
