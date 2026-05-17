package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detalle completo de un cliente para la pantalla "Clientes y Préstamos":
 * datos del cliente, resumen de su cartera, préstamo activo e historial de
 * pagos recientes.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClienteDetalleDto {

    private Long id;
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private String barrio;
    private String observacion;
    private String estado;
    private Boolean activo;
    private LocalDate fecha_registro;
    private String ruta;

    /** Total de préstamos del cliente (cualquier estado). */
    private Integer prestamos_totales;
    /** Préstamos en estado ACTIVO. */
    private Integer prestamos_activos;
    /** Días de mora del préstamo activo (0 si está al día). */
    private Integer dias_mora;

    /** Suma de los pagos APLICADOS del cliente. */
    private BigDecimal total_pagado;
    /** Cuotas pagadas del préstamo activo. */
    private Integer cuotas_pagadas;
    /** Cuotas totales del préstamo activo. */
    private Integer cuotas_totales;

    /** Préstamo activo más reciente; null si el cliente no tiene. */
    private ClientePrestamoDto prestamo_activo;

    /** Últimos pagos registrados del cliente. */
    private List<ClientePagoDto> pagos_recientes;
}
