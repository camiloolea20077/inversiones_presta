package com.cloud_technological.inversiones_prestar.services;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoListDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionResponseDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface PrestamoService {

    PrestamoResponseDto crear(PrestamoRequestDto dto);

    /**
     * Crea un cliente nuevo, lo inserta en la ruta tras un cliente base,
     * le crea el préstamo, genera las cuotas y actualiza la planilla diaria.
     * Toda la operación es transaccional (HU-BE-014).
     */
    ClienteConPrestamoResponseDto crearClienteConPrestamo(ClienteConPrestamoRequestDto dto);

    PrestamoResponseDto obtener(Long id);

    Page<PrestamoListDto> listar(PageableDto<Object> pageable);

    SimulacionResponseDto simular(SimulacionRequestDto dto);
}
