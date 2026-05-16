package com.cloud_technological.inversiones_prestar.repositories.prestamos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.CuotaPrestamoEntity;

@Repository
public interface CuotaPrestamoJPARepository extends JpaRepository<CuotaPrestamoEntity, Long> {

    List<CuotaPrestamoEntity> findByPrestamoIdAndDeletedAtIsNullOrderByNumeroCuotaAsc(Long prestamoId);
}
