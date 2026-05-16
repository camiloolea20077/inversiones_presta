package com.cloud_technological.inversiones_prestar.repositories.trabajadores;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;

@Repository
public interface TrabajadorJPARepository extends JpaRepository<TrabajadorEntity, Long> {

    Optional<TrabajadorEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<TrabajadorEntity> findByDocumentoAndDeletedAtIsNull(String documento);

    Optional<TrabajadorEntity> findByUsuarioIdAndDeletedAtIsNull(Long usuarioId);

    List<TrabajadorEntity> findByActivoTrueAndDeletedAtIsNullOrderByNombreAsc();
}
