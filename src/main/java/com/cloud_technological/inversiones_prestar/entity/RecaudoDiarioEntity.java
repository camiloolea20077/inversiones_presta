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

/** Planilla diaria de recaudo de una ruta. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recaudos_diarios")
public class RecaudoDiarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "fecha_recaudo", nullable = false)
    private LocalDate fechaRecaudo;

    @Column(name = "total_esperado", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalEsperado;

    @Column(name = "total_recaudado", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalRecaudado;

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
        if (totalEsperado == null) {
            totalEsperado = BigDecimal.ZERO;
        }
        if (totalRecaudado == null) {
            totalRecaudado = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = "ABIERTO";
        }
    }
}
