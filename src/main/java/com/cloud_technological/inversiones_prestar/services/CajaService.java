package com.cloud_technological.inversiones_prestar.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.caja.AbrirCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaDiariaResponseDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaListDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CerrarCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaListDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

public interface CajaService {

    /** Abre la caja diaria de un trabajador con un valor inicial (HU-BE-015). */
    CajaDiariaResponseDto abrir(AbrirCajaRequestDto dto);

    /** Cierra la caja registrando el valor entregado y calculando la diferencia. */
    CajaDiariaResponseDto cerrar(CerrarCajaRequestDto dto);

    /** Caja por id. */
    CajaDiariaResponseDto obtener(Long id);

    /**
     * Listado paginado de cajas diarias de los trabajadores con filtros
     * opcionales por trabajador, ruta, estado y fecha (HU-FE-020).
     */
    Page<CajaListDto> listar(PageableDto<Object> pageable);

    /** Caja vigente de un trabajador para una fecha (null = caja del día). */
    CajaDiariaResponseDto obtenerPorTrabajador(Long trabajadorId, LocalDate fecha);

    /** Caja diaria del trabajador autenticado (HU-FE-011). */
    CajaDiariaResponseDto miCaja(LocalDate fecha);

    /**
     * Movimientos de una caja diaria en orden cronológico (HU-BE-016).
     * Trazabilidad del dinero del trabajador.
     */
    List<MovimientoCajaDto> movimientosPorCaja(Long cajaId);

    /**
     * Listado paginado de movimientos de caja con filtros opcionales por caja,
     * trabajador, ruta, tipo de movimiento y rango de fechas (HU-BE-016).
     */
    Page<MovimientoCajaListDto> listarMovimientos(PageableDto<Object> pageable);

    /**
     * Registra un préstamo entregado en la caja del trabajador para la fecha
     * dada, si existe una caja abierta. Suma a valor_prestamos_entregados y
     * genera el movimiento PRESTAMO_ENTREGADO. Si no hay caja abierta, no hace
     * nada (la operación de préstamo no debe bloquearse por la caja).
     */
    void registrarPrestamoEntregado(Long trabajadorId, Long rutaId, LocalDate fecha,
            BigDecimal valor, Long prestamoId, Long usuarioId);

    /**
     * Registra un pago recibido en la caja del trabajador para la fecha dada,
     * si existe una caja abierta. Suma a valor_recaudado y genera el movimiento
     * PAGO_RECIBIDO.
     */
    void registrarPagoRecibido(Long trabajadorId, Long rutaId, LocalDate fecha,
            BigDecimal valor, Long pagoId, Long usuarioId);
}
