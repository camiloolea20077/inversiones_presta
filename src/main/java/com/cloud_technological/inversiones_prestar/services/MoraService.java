package com.cloud_technological.inversiones_prestar.services;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.mora.MoraListDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface MoraService {

    /** Listado paginado del reporte de clientes en mora (HU-BE-019). */
    Page<MoraListDto> listar(PageableDto<Object> pageable);
}
