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
@Table(name = "limites_trabajador")
public class LimiteTrabajadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "monto_maximo_prestamo", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoMaximoPrestamo;

    @Column(name = "tasa_minima", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaMinima;

    @Column(name = "tasa_maxima", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaMaxima;

    @Column(name = "plazo_maximo_dias", nullable = false)
    private Integer plazoMaximoDias;

    @Column(name = "puede_crear_cliente", nullable = false)
    private Boolean puedeCrearCliente;

    @Column(name = "puede_crear_prestamo", nullable = false)
    private Boolean puedeCrearPrestamo;

    @Column(name = "puede_definir_tasa", nullable = false)
    private Boolean puedeDefinirTasa;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

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
        if (activo == null) {
            activo = true;
        }
    }
}
