package com.cloud_technological.inversiones_prestar.dto.prestamos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Solicitud para crear un cliente nuevo y, en la misma operación
 * transaccional, su préstamo (HU-BE-014).
 */
@Getter
@Setter
public class ClienteConPrestamoRequestDto {

    /* ===================== Datos del cliente ===================== */

    @Size(max = 30, message = "El documento no puede superar 30 caracteres")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccion;

    @Size(max = 150, message = "El barrio no puede superar 150 caracteres")
    private String barrio;

    private String observacionCliente;

    /* ===================== Ubicación en la ruta ===================== */

    @NotNull(message = "La ruta es obligatoria")
    private Long rutaId;

    /**
     * Orden del cliente base después del cual se insertará el nuevo cliente.
     * Si es null, el cliente se agrega al final del recorrido.
     */
    private Integer ordenBase;

    /* ===================== Datos del préstamo ===================== */

    @NotNull(message = "El trabajador es obligatorio")
    private Long trabajadorId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1", message = "El monto debe ser mayor a cero")
    private BigDecimal montoPrestado;

    @NotNull(message = "La tasa es obligatoria")
    @DecimalMin(value = "0", message = "La tasa no puede ser negativa")
    private BigDecimal tasaPorcentaje;

    /** FIJO_PRESTAMO, MENSUAL o DIARIO. Por defecto FIJO_PRESTAMO. */
    private String tipoInteres;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 día")
    private Integer plazoDias;

    private String observacionPrestamo;
}
