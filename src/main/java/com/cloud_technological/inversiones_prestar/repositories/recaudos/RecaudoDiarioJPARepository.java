package com.cloud_technological.inversiones_prestar.repositories.recaudos;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RecaudoDiarioEntity;

@Repository
public interface RecaudoDiarioJPARepository extends JpaRepository<RecaudoDiarioEntity, Long> {

    Optional<RecaudoDiarioEntity> findByIdAndDeletedAtIsNull(Long id);

    /** Recaudo vigente (no anulado) de una ruta para una fecha. */
    Optional<RecaudoDiarioEntity> findFirstByRutaIdAndFechaRecaudoAndEstadoNotAndDeletedAtIsNull(
            Long rutaId, LocalDate fechaRecaudo, String estado);
}
