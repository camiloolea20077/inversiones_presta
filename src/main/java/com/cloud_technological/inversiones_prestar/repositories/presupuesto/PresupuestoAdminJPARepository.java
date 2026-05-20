package com.cloud_technological.inversiones_prestar.repositories.presupuesto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.PresupuestoAdminEntity;

@Repository
public interface PresupuestoAdminJPARepository extends JpaRepository<PresupuestoAdminEntity, Long> {

    Optional<PresupuestoAdminEntity> findByIdAndDeletedAtIsNull(Long id);

    /** Presupuesto activo único del administrador. */
    Optional<PresupuestoAdminEntity> findFirstByEstadoAndDeletedAtIsNull(String estado);
}
