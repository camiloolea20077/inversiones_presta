package com.cloud_technological.inversiones_prestar.services;

import java.time.LocalDate;

import com.cloud_technological.inversiones_prestar.dto.dashboard.DashboardResumenDto;

public interface DashboardService {

    /** Indicadores generales del negocio para la fecha indicada (HU-BE-018). */
    DashboardResumenDto resumen(LocalDate fecha);
}
