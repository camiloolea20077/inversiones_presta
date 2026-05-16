package com.cloud_technological.inversiones_prestar.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorComboDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorListDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorResponseDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface TrabajadorService {

    TrabajadorResponseDto crear(TrabajadorRequestDto dto);

    TrabajadorResponseDto actualizar(Long id, TrabajadorRequestDto dto);

    TrabajadorResponseDto obtener(Long id);

    Page<TrabajadorListDto> listar(PageableDto<Object> pageable);

    List<TrabajadorComboDto> listarActivos();

    TrabajadorResponseDto cambiarEstado(Long id, Boolean activo);
}
