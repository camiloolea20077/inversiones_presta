package com.cloud_technological.inversiones_prestar.dto.caja;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Solicitud para abrir la caja diaria de un trabajador. */
@Getter
@Setter
public class AbrirCajaRequestDto {

    @NotNull(message = "El trabajador es obligatorio")
    private Long trabajadorId;

    /** Ruta de la caja. Si es null, se toma la ruta vigente del trabajador. */
    private Long rutaId;

    @NotNull(message = "El valor inicial es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor inicial no puede ser negativo")
    private BigDecimal valorInicial;

    /** Fecha de la caja. Si es null, se usa la fecha actual. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private String observacion;
}
