package com.cloud_technological.inversiones_prestar.dto.recaudos;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecaudoDetalleDto {

    private Long id;
    private Integer orden;
    private Long clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteDireccion;
    private Long prestamoId;
    private BigDecimal valorEsperado;
    private BigDecimal valorPagado;
    private BigDecimal saldoPendiente;
    private String estado;
    private String observacion;
}
