package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PrestamoResponseDto {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long rutaId;
    private String rutaNombre;
    private Long trabajadorId;
    private String trabajadorNombre;
    private BigDecimal montoPrestado;
    private BigDecimal tasaPorcentaje;
    private String tipoInteres;
    private Integer plazoDias;
    private BigDecimal valorInteres;
    private BigDecimal totalPagar;
    private BigDecimal cuotaDiaria;
    private BigDecimal saldoActual;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String observacion;
    private List<CuotaPrestamoDto> cuotas;
}
