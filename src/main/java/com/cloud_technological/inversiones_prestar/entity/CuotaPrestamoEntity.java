package com.cloud_technological.inversiones_prestar.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "cuotas_prestamo")
public class CuotaPrestamoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prestamo_id", nullable = false)
    private Long prestamoId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_cuota", nullable = false)
    private LocalDate fechaCuota;

    @Column(name = "valor_cuota", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorCuota;

    @Column(name = "valor_pagado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorPagado;

    @Column(name = "saldo_cuota", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoCuota;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

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
        if (valorPagado == null) {
            valorPagado = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
