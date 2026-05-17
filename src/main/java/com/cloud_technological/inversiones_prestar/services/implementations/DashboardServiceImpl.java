package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.dashboard.DashboardResumenDto;
import com.cloud_technological.inversiones_prestar.dto.dashboard.DashboardRutaDto;
import com.cloud_technological.inversiones_prestar.repositories.dashboard.DashboardQueryRepository;
import com.cloud_technological.inversiones_prestar.services.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenDto resumen(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();

        BigDecimal recaudoEsperado = dashboardQueryRepository.recaudoEsperado(dia);
        BigDecimal recaudoReal = dashboardQueryRepository.recaudoReal(dia);
        BigDecimal diferencia = recaudoEsperado.subtract(recaudoReal);

        BigDecimal cumplimiento = BigDecimal.ZERO;
        if (recaudoEsperado.compareTo(BigDecimal.ZERO) > 0) {
            cumplimiento = recaudoReal
                    .multiply(BigDecimal.valueOf(100))
                    .divide(recaudoEsperado, 2, RoundingMode.HALF_UP);
        }

        List<DashboardRutaDto> resumenRutas = dashboardQueryRepository.resumenRutas(dia);

        return DashboardResumenDto.builder()
                .totalPrestado(dashboardQueryRepository.totalPrestado())
                .totalRecaudadoHoy(dashboardQueryRepository.totalRecaudado(dia))
                .carteraActiva(dashboardQueryRepository.carteraActiva())
                .clientesEnMora(dashboardQueryRepository.clientesEnMora(dia))
                .rutasActivas(dashboardQueryRepository.rutasActivas())
                .trabajadoresActivos(dashboardQueryRepository.trabajadoresActivos())
                .recaudoEsperado(recaudoEsperado)
                .recaudoReal(recaudoReal)
                .cumplimientoPorcentaje(cumplimiento)
                .diferenciaRecaudo(diferencia)
                .resumenRutas(resumenRutas)
                .build();
    }
}
