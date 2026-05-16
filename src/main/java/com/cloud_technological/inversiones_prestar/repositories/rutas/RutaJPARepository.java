package com.cloud_technological.inversiones_prestar.repositories.rutas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RutaEntity;

@Repository
public interface RutaJPARepository extends JpaRepository<RutaEntity, Long> {

    Optional<RutaEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<RutaEntity> findByNombreAndDeletedAtIsNull(String nombre);

    List<RutaEntity> findByActivoTrueAndDeletedAtIsNullOrderByNombreAsc();
}
