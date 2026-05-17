package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Estado completo de la caja diaria de un trabajador. */
@Getter
@Setter
@Builder
public class CajaDiariaResponseDto {

    private Long id;
    private Long trabajadorId;
    private String trabajadorNombre;
    private Long rutaId;
    private String rutaNombre;
    private LocalDate fechaCaja;

    private BigDecimal valorInicial;
    private BigDecimal valorPrestamosEntregados;
    private BigDecimal valorRecaudado;
    private BigDecimal valorEsperadoCierre;
    private BigDecimal valorEntregado;
    private BigDecimal diferencia;

    private String estado;
    private String observacion;

    private List<MovimientoCajaDto> movimientos;
}
