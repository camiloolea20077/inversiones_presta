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

/**
 * Evento de auditoría del sistema (HU-BE-020): registra acciones sensibles
 * (creación de clientes/préstamos, pagos, anulaciones, cambios de orden en
 * ruta, cierres de caja) con usuario, acción, tabla y registro afectado, y
 * los valores anterior/nuevo serializados como JSON (texto).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eventos_auditoria")
public class EventoAuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nombre", length = 150)
    private String usuarioNombre;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "tabla_afectada", nullable = false, length = 80)
    private String tablaAfectada;

    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "valor_anterior", columnDefinition = "text")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "text")
    private String valorNuevo;

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
    }
}
