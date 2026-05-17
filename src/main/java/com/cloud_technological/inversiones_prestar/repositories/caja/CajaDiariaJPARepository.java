package com.cloud_technological.inversiones_prestar.repositories.caja;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.CajaDiariaEntity;

@Repository
public interface CajaDiariaJPARepository extends JpaRepository<CajaDiariaEntity, Long> {

    Optional<CajaDiariaEntity> findByIdAndDeletedAtIsNull(Long id);

    /** Caja vigente (no anulada) de un trabajador para una fecha. */
    Optional<CajaDiariaEntity> findFirstByTrabajadorIdAndFechaCajaAndEstadoNotAndDeletedAtIsNull(
            Long trabajadorId, LocalDate fechaCaja, String estado);
}
