package com.cloud_technological.inversiones_prestar.repositories.caja;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.MovimientoCajaEntity;

@Repository
public interface MovimientoCajaJPARepository extends JpaRepository<MovimientoCajaEntity, Long> {

    List<MovimientoCajaEntity> findByCajaDiariaIdAndDeletedAtIsNullOrderByFechaMovimientoAsc(
            Long cajaDiariaId);
}
