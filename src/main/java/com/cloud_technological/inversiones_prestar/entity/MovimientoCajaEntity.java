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

/** Movimiento de caja: trazabilidad del dinero del trabajador (HU-BE-016). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCajaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caja_diaria_id", nullable = false)
    private Long cajaDiariaId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    private String tipoMovimiento;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    private BigDecimal valor;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

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
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }
}
