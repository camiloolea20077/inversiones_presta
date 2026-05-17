package com.cloud_technological.inversiones_prestar.dto.auditoria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario que tiene eventos en el registro de auditoría (HU-FE-022).
 * Alimenta el filtro "por usuario" del reporte de auditoría.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaUsuarioDto {

    private Long id;
    private String nombre;
}
