package com.cloud_technological.inversiones_prestar.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteComboDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteDetalleDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteListDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteRequestDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteResponseDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface ClienteService {

    ClienteResponseDto crear(ClienteRequestDto dto);

    ClienteResponseDto actualizar(Long id, ClienteRequestDto dto);

    ClienteResponseDto obtener(Long id);

    /** Detalle completo del cliente: datos, cartera, préstamo activo y pagos. */
    ClienteDetalleDto detalle(Long id);

    Page<ClienteListDto> listar(PageableDto<Object> pageable);

    List<ClienteComboDto> listarActivos();

    ClienteResponseDto cambiarEstado(Long id, String estado);
}
