package com.cloud_technological.inversiones_prestar.dto.recaudos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerarRecaudoDto {

    @NotNull(message = "La ruta es obligatoria")
    private Long rutaId;

    /** Fecha del recaudo. Si es null, se usa la fecha actual. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;
}
