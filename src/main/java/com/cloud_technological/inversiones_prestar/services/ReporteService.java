package com.cloud_technological.inversiones_prestar.services;

import java.time.LocalDate;

import com.cloud_technological.inversiones_prestar.dto.reportes.RentabilidadDto;

public interface ReporteService {

    /** Reporte de rentabilidad consolidado a la fecha indicada. */
    RentabilidadDto rentabilidad(LocalDate fechaCorte);
}
