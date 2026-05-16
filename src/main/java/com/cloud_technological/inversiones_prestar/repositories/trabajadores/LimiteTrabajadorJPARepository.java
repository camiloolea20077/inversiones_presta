package com.cloud_technological.inversiones_prestar.repositories.trabajadores;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.LimiteTrabajadorEntity;

@Repository
public interface LimiteTrabajadorJPARepository extends JpaRepository<LimiteTrabajadorEntity, Long> {

    Optional<LimiteTrabajadorEntity> findByTrabajadorIdAndActivoTrueAndDeletedAtIsNull(Long trabajadorId);
}
