package com.cloud_technological.inversiones_prestar.repositories.clientes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.RutaClienteEntity;

@Repository
public interface RutaClienteJPARepository extends JpaRepository<RutaClienteEntity, Long> {

    Optional<RutaClienteEntity> findByIdAndDeletedAtIsNull(Long id);

    List<RutaClienteEntity> findByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByOrdenAsc(Long rutaId);

    Optional<RutaClienteEntity> findByRutaIdAndClienteIdAndActivoTrueAndDeletedAtIsNull(
            Long rutaId, Long clienteId);
}
