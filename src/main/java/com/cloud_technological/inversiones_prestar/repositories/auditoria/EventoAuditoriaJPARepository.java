package com.cloud_technological.inversiones_prestar.repositories.auditoria;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.entity.EventoAuditoriaEntity;

@Repository
public interface EventoAuditoriaJPARepository extends JpaRepository<EventoAuditoriaEntity, Long> {

    Optional<EventoAuditoriaEntity> findByIdAndDeletedAtIsNull(Long id);
}
