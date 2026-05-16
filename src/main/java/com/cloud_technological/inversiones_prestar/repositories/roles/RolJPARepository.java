package com.cloud_technological.inversiones_prestar.repositories.roles;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RolEntity;

@Repository
public interface RolJPARepository extends JpaRepository<RolEntity, Long> {

    Optional<RolEntity> findByNombre(String nombre);
}
