package com.cloud_technological.inversiones_prestar.repositories.clientes;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.ClienteEntity;

@Repository
public interface ClienteJPARepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<ClienteEntity> findByDocumentoAndDeletedAtIsNull(String documento);
}
