package com.cloud_technological.inversiones_prestar.repositories.prestamos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.PrestamoEntity;

@Repository
public interface PrestamoJPARepository extends JpaRepository<PrestamoEntity, Long> {

    Optional<PrestamoEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<PrestamoEntity> findFirstByClienteIdAndEstadoAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long clienteId, String estado);
}
