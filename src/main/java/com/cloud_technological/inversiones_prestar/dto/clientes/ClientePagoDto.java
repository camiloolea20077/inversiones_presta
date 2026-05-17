package com.cloud_technological.inversiones_prestar.dto.clientes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pago reciente del cliente, para el historial del panel de detalle de la
 * pantalla "Clientes y Préstamos".
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientePagoDto {

    private Long id;
    private LocalDateTime fecha_pago;
    private BigDecimal valor_pago;
    private String forma_pago;
    private String estado;
}
