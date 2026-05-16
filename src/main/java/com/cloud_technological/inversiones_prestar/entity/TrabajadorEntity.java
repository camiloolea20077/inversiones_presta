package com.cloud_technological.inversiones_prestar.entity;

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
@Table(name = "trabajadores")
public class TrabajadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario del sistema asociado al trabajador (opcional). */
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "documento", nullable = false, length = 30)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

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
