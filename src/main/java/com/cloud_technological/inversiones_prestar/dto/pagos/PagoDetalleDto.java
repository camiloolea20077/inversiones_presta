package com.cloud_technological.inversiones_prestar.dto.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Detalle de un pago registrado (HU-FE-019).
 */
@Getter
@Setter
@Builder
public class PagoDetalleDto {

    private Long id;
    private LocalDateTime fechaPago;

    private Long clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteTelefono;

    private Long trabajadorId;
    private String trabajadorNombre;

    private Long rutaId;
    private String rutaNombre;

    private Long prestamoId;
    private BigDecimal prestamoMonto;
    private BigDecimal prestamoTotalPagar;
    private BigDecimal prestamoSaldoActual;
    private String prestamoEstado;

    private Long cuotaPrestamoId;
    private Integer numeroCuota;

    private Long recaudoDetalleId;

    private BigDecimal valorPago;
    private String formaPago;
    private String estado;
    private String observacion;
}
