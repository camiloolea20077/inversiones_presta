package com.cloud_technological.inversiones_prestar.dto.recaudos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecaudoDiarioResponseDto {

    private Long id;
    private Long rutaId;
    private String rutaNombre;
    private Long trabajadorId;
    private String trabajadorNombre;
    private LocalDate fechaRecaudo;
    private BigDecimal totalEsperado;
    private BigDecimal totalRecaudado;
    private String estado;

    private Integer totalClientes;
    private Integer clientesPagados;
    private Integer clientesPendientes;
    private Integer clientesParciales;
    private Integer clientesNoPago;

    private List<RecaudoDetalleDto> detalles;
}
