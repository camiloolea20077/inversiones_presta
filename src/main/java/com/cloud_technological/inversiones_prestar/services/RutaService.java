package com.cloud_technological.inversiones_prestar.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.rutas.RutaComboDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaListDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaResponseDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface RutaService {

    RutaResponseDto crear(RutaRequestDto dto);

    RutaResponseDto actualizar(Long id, RutaRequestDto dto);

    RutaResponseDto obtener(Long id);

    Page<RutaListDto> listar(PageableDto<Object> pageable);

    List<RutaComboDto> listarActivas();

    RutaResponseDto cambiarEstado(Long id, Boolean activo);

    RutaResponseDto asignarTrabajador(Long id, Long trabajadorId);
}
