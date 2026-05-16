package com.cloud_technological.inversiones_prestar.services;

import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorResponseDto;

public interface LimiteTrabajadorService {

    /** Devuelve el límite activo del trabajador, o {@code null} si no tiene. */
    LimiteTrabajadorResponseDto obtenerPorTrabajador(Long trabajadorId);

    /** Guarda una nueva configuración de límites cerrando la anterior si existe. */
    LimiteTrabajadorResponseDto guardar(Long trabajadorId, LimiteTrabajadorRequestDto dto);
}
