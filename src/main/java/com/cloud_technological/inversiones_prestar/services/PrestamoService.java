package com.cloud_technological.inversiones_prestar.services;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoListDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionResponseDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface PrestamoService {

    PrestamoResponseDto crear(PrestamoRequestDto dto);

    PrestamoResponseDto obtener(Long id);

    Page<PrestamoListDto> listar(PageableDto<Object> pageable);

    SimulacionResponseDto simular(SimulacionRequestDto dto);
}
