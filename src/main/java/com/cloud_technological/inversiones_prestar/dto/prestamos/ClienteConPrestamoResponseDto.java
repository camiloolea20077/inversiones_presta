package com.cloud_technological.inversiones_prestar.dto.prestamos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Resultado de crear cliente + préstamo en una sola operación (HU-BE-014).
 */
@Getter
@Setter
@Builder
public class ClienteConPrestamoResponseDto {

    private Long clienteId;
    private String clienteNombre;

    /** Orden final del nuevo cliente dentro de la ruta. */
    private Integer ordenAsignado;

    private PrestamoResponseDto prestamo;

    /** Id del recaudo diario actualizado, null si no había planilla del día. */
    private Long recaudoDiarioId;

    /** true si se actualizó la planilla diaria existente. */
    private boolean planillaActualizada;
}
