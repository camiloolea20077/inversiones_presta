package com.cloud_technological.inversiones_prestar.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prestamo_id", nullable = false)
    private Long prestamoId;

    @Column(name = "cuota_prestamo_id")
    private Long cuotaPrestamoId;

    @Column(name = "recaudo_detalle_id")
    private Long recaudoDetalleId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "valor_pago", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "forma_pago", nullable = false, length = 30)
    private String formaPago;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "APLICADO";
        }
    }
}
