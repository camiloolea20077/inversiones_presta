package com.cloud_technological.inversiones_prestar.repositories.pagos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.PagoEntity;

@Repository
public interface PagoJPARepository extends JpaRepository<PagoEntity, Long> {
}
