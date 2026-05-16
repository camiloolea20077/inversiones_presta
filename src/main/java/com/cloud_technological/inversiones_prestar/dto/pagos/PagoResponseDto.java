package com.cloud_technological.inversiones_prestar.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PagoResponseDto {

    private Long id;
    private Long prestamoId;
    private Long recaudoDetalleId;
    private Long clienteId;
    private String clienteNombre;
    private BigDecimal valorPago;
    private String formaPago;
    private String estado;
    private LocalDateTime fechaPago;

    /** Estado resultante del renglón del recaudo (PAGADO / PARCIAL / NO_PAGO). */
    private String detalleEstado;
    /** Saldo del préstamo después del pago. */
    private BigDecimal saldoPrestamo;
    private String prestamoEstado;
}
