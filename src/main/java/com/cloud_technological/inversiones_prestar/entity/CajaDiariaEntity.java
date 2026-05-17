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

/** Caja diaria del trabajador: control del dinero recibido, prestado y recaudado. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cajas_diarias")
public class CajaDiariaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "fecha_caja", nullable = false)
    private LocalDate fechaCaja;

    @Column(name = "valor_inicial", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_prestamos_entregados", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorPrestamosEntregados;

    @Column(name = "valor_recaudado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorRecaudado;

    @Column(name = "valor_esperado_cierre", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorEsperadoCierre;

    @Column(name = "valor_entregado", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorEntregado;

    @Column(name = "diferencia", nullable = false, precision = 18, scale = 2)
    private BigDecimal diferencia;

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
        if (valorInicial == null) {
            valorInicial = BigDecimal.ZERO;
        }
        if (valorPrestamosEntregados == null) {
            valorPrestamosEntregados = BigDecimal.ZERO;
        }
        if (valorRecaudado == null) {
            valorRecaudado = BigDecimal.ZERO;
        }
        if (valorEsperadoCierre == null) {
            valorEsperadoCierre = valorInicial;
        }
        if (valorEntregado == null) {
            valorEntregado = BigDecimal.ZERO;
        }
        if (diferencia == null) {
            diferencia = BigDecimal.ZERO;
        }
        if (estado == null) {
            estado = "ABIERTA";
        }
    }
}
