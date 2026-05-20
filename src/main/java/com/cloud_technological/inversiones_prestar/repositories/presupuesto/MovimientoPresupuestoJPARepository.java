package com.cloud_technological.inversiones_prestar.repositories.presupuesto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.MovimientoPresupuestoEntity;

@Repository
public interface MovimientoPresupuestoJPARepository
        extends JpaRepository<MovimientoPresupuestoEntity, Long> {

    List<MovimientoPresupuestoEntity> findByPresupuestoIdAndDeletedAtIsNullOrderByFechaMovimientoDesc(
            Long presupuestoId);
}
