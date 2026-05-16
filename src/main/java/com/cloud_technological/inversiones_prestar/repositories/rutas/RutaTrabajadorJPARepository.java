package com.cloud_technological.inversiones_prestar.repositories.rutas;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RutaTrabajadorEntity;

@Repository
public interface RutaTrabajadorJPARepository extends JpaRepository<RutaTrabajadorEntity, Long> {

    /** Asignación activa actual de una ruta. */
    Optional<RutaTrabajadorEntity> findFirstByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(
            Long rutaId);

    /** Asignación activa actual de un trabajador (su ruta vigente). */
    Optional<RutaTrabajadorEntity> findFirstByTrabajadorIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(
            Long trabajadorId);
}
