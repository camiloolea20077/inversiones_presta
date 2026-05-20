package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.reportes.RentabilidadDto;
import com.cloud_technological.inversiones_prestar.entity.PresupuestoAdminEntity;
import com.cloud_technological.inversiones_prestar.repositories.presupuesto.PresupuestoAdminJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.presupuesto.PresupuestoQueryRepository;
import com.cloud_technological.inversiones_prestar.services.ReporteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PresupuestoQueryRepository presupuestoQueryRepository;
    private final PresupuestoAdminJPARepository presupuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public RentabilidadDto rentabilidad(LocalDate fechaCorte) {
        LocalDate fecha = fechaCorte != null ? fechaCorte : LocalDate.now();

        BigDecimal capitalAportado = presupuestoRepository
                .findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .map(PresupuestoAdminEntity::getId)
                .map(presupuestoQueryRepository::capitalAportado)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalPrestado = presupuestoQueryRepository.totalPrestadoHistorico();
        BigDecimal totalRecaudado = presupuestoQueryRepository.totalRecaudadoHistorico();
        BigDecimal saldoDisponible = escala(
                capitalAportado.subtract(totalPrestado).add(totalRecaudado));

        BigDecimal capitalEnCalle = presupuestoQueryRepository.capitalEnCalle();
        BigDecimal capitalRecuperado = escala(totalPrestado.subtract(capitalEnCalle));

        BigDecimal carteraActiva = presupuestoQueryRepository.carteraActiva();
        Long prestamosActivos = presupuestoQueryRepository.prestamosActivos();
        Long prestamosPagados = presupuestoQueryRepository.prestamosPagados();

        BigDecimal interesesProyectados = presupuestoQueryRepository.interesesProyectadosTotales();
        BigDecimal interesesCobrados = presupuestoQueryRepository.interesesCobrados();
        BigDecimal interesesPorCobrar = escala(interesesProyectados.subtract(interesesCobrados));

        BigDecimal rentRealizada = porcentaje(interesesCobrados, capitalAportado);
        BigDecimal rentProyectada = porcentaje(interesesProyectados, capitalAportado);

        BigDecimal totalEnMora = presupuestoQueryRepository.totalEnMora(fecha);
        Long clientesEnMora = presupuestoQueryRepository.clientesEnMora(fecha);
        BigDecimal porcentajeMora = porcentaje(totalEnMora, carteraActiva);

        return RentabilidadDto.builder()
                .fechaCorte(fecha)
                .capitalAportado(escala(capitalAportado))
                .saldoDisponible(saldoDisponible)
                .capitalPrestadoHistorico(escala(totalPrestado))
                .capitalEnCalle(escala(capitalEnCalle))
                .capitalRecuperado(capitalRecuperado)
                .carteraActiva(escala(carteraActiva))
                .prestamosActivos(prestamosActivos)
                .prestamosPagados(prestamosPagados)
                .interesesProyectadosTotales(escala(interesesProyectados))
                .interesesCobrados(escala(interesesCobrados))
                .interesesPorCobrar(interesesPorCobrar)
                .rentabilidadRealizadaPorcentaje(rentRealizada)
                .rentabilidadProyectadaPorcentaje(rentProyectada)
                .totalEnMora(escala(totalEnMora))
                .clientesEnMora(clientesEnMora)
                .porcentajeMora(porcentajeMora)
                .build();
    }

    private BigDecimal porcentaje(BigDecimal numerador, BigDecimal denominador) {
        if (denominador == null || denominador.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerador.multiply(CIEN).divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
