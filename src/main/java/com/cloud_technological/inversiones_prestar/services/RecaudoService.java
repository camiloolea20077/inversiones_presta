package com.cloud_technological.inversiones_prestar.services;

import java.time.LocalDate;

import com.cloud_technological.inversiones_prestar.dto.recaudos.RecaudoDiarioResponseDto;

public interface RecaudoService {

    /** Genera (o devuelve si ya existe) la planilla diaria de una ruta. */
    RecaudoDiarioResponseDto generar(Long rutaId, LocalDate fecha);

    RecaudoDiarioResponseDto obtener(Long id);

    /** Ruta diaria del trabajador autenticado para la fecha indicada. */
    RecaudoDiarioResponseDto miRutaDiaria(LocalDate fecha);
}
