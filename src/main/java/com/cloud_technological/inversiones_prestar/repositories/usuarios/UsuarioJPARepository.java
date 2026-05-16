package com.cloud_technological.inversiones_prestar.repositories.usuarios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.UsuarioEntity;

@Repository
public interface UsuarioJPARepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmailAndDeletedAtIsNull(String email);

    Optional<UsuarioEntity> findByUsuarioAndDeletedAtIsNull(String usuario);
}
