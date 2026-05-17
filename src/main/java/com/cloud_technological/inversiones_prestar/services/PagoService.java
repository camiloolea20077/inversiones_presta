package com.cloud_technological.inversiones_prestar.services;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.pagos.MarcarNoPagoDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoDetalleDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoListDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.RegistrarPagoDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface PagoService {

    /** Registra un pago (total o parcial) sobre un renglón del recaudo. */
    PagoResponseDto registrarPago(RegistrarPagoDto dto);

    /** Marca que el cliente no pagó en la visita del día. */
    PagoResponseDto marcarNoPago(MarcarNoPagoDto dto);

    /** Listado paginado de pagos con filtros por fecha, trabajador y ruta (HU-FE-019). */
    Page<PagoListDto> listar(PageableDto<Object> pageable);

    /** Detalle de un pago registrado (HU-FE-019). */
    PagoDetalleDto obtener(Long id);
}
