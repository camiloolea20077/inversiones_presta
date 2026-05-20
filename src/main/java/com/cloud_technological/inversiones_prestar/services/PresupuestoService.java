package com.cloud_technological.inversiones_prestar.services;

import java.math.BigDecimal;

import com.cloud_technological.inversiones_prestar.dto.presupuesto.AbrirPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.MovimientoPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.PresupuestoResponseDto;

public interface PresupuestoService {

    /** Abre por primera vez el presupuesto del administrador. */
    PresupuestoResponseDto abrir(AbrirPresupuestoRequestDto dto);

    /** Agrega capital al presupuesto activo. */
    PresupuestoResponseDto capitalizar(MovimientoPresupuestoRequestDto dto);

    /** Retira capital del presupuesto activo (no puede dejar el saldo en negativo). */
    PresupuestoResponseDto retirar(MovimientoPresupuestoRequestDto dto);

    /** Devuelve el estado del presupuesto activo (con saldo disponible calculado). */
    PresupuestoResponseDto obtenerActivo();

    /**
     * Saldo disponible actual del administrador. Si no hay presupuesto activo,
     * se considera 0 (lo cual bloquea cualquier préstamo).
     */
    BigDecimal saldoDisponible();

    /** Resta el monto al saldo disponible disparando error si lo excede. */
    void validarYDescontarPrestamo(BigDecimal montoSolicitado);
}
