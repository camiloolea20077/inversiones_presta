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

/** Renglón de la planilla diaria: un cliente del recorrido. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recaudo_detalles")
public class RecaudoDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recaudo_diario_id", nullable = false)
    private Long recaudoDiarioId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "prestamo_id")
    private Long prestamoId;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "valor_esperado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorEsperado;

    @Column(name = "valor_pagado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorPagado;

    @Column(name = "saldo_pendiente", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoPendiente;

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
        if (valorEsperado == null) {
            valorEsperado = BigDecimal.ZERO;
        }
        if (valorPagado == null) {
            valorPagado = BigDecimal.ZERO;
        }
        if (saldoPendiente == null) {
            saldoPendiente = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
