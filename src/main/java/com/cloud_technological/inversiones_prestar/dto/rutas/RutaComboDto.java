package com.cloud_technological.inversiones_prestar.dto.rutas;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Ruta en formato reducido para selects/combos. */
@Getter
@Setter
@Builder
public class RutaComboDto {

    private Long id;
    private String nombre;
    private String zona;
}
