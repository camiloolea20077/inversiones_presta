package com.cloud_technological.inversiones_prestar.services;

import com.cloud_technological.inversiones_prestar.dto.pagos.MarcarNoPagoDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.RegistrarPagoDto;

public interface PagoService {

    /** Registra un pago (total o parcial) sobre un renglón del recaudo. */
    PagoResponseDto registrarPago(RegistrarPagoDto dto);

    /** Marca que el cliente no pagó en la visita del día. */
    PagoResponseDto marcarNoPago(MarcarNoPagoDto dto);
}
