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
@Table(name = "prestamos")
public class PrestamoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "trabajador_id", nullable = false)
    private Long trabajadorId;

    @Column(name = "monto_prestado", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoPrestado;

    @Column(name = "tasa_porcentaje", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaPorcentaje;

    @Column(name = "tipo_interes", nullable = false, length = 50)
    private String tipoInteres;

    @Column(name = "plazo_dias", nullable = false)
    private Integer plazoDias;

    @Column(name = "valor_interes", nullable = false, precision = 18, scale = 2)
    private BigDecimal valorInteres;

    @Column(name = "total_pagar", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPagar;

    @Column(name = "cuota_diaria", nullable = false, precision = 18, scale = 2)
    private BigDecimal cuotaDiaria;

    @Column(name = "saldo_actual", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoActual;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

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
        if (estado == null) {
            estado = "ACTIVO";
        }
    }
}
