package com.cloud_technological.inversiones_prestar.repositories.recaudos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RecaudoDetalleEntity;

@Repository
public interface RecaudoDetalleJPARepository extends JpaRepository<RecaudoDetalleEntity, Long> {

    List<RecaudoDetalleEntity> findByRecaudoDiarioIdAndDeletedAtIsNullOrderByOrdenAsc(Long recaudoDiarioId);

    Optional<RecaudoDetalleEntity> findByIdAndDeletedAtIsNull(Long id);
}
