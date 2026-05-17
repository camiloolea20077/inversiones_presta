package com.cloud_technological.inversiones_prestar.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaListDto;
import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaUsuarioDto;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Auditoría general del sistema (HU-BE-020). Registra acciones sensibles
 * (creación de clientes/préstamos, pagos, anulaciones, cambios de orden en
 * ruta, cierres de caja) y permite consultarlas.
 */
public interface AuditoriaService {

    /**
     * Registra un evento de auditoría. El registro es defensivo: si falla,
     * NO debe romper la operación de negocio que lo invocó.
     *
     * @param accion         acción ejecutada (CREAR, PAGAR, ANULAR, ...)
     * @param tablaAfectada  tabla / entidad afectada
     * @param registroId     id del registro afectado (puede ser null)
     * @param valorAnterior  estado anterior (se serializa a JSON); puede ser null
     * @param valorNuevo     estado nuevo (se serializa a JSON); puede ser null
     * @param observacion    descripción legible del evento
     */
    void registrar(String accion, String tablaAfectada, Long registroId,
            Object valorAnterior, Object valorNuevo, String observacion);

    /** Listado paginado de eventos de auditoría con filtros (HU-FE-022). */
    Page<AuditoriaListDto> listar(PageableDto<Object> pageable);

    /** Usuarios con eventos de auditoría, para alimentar el filtro por usuario. */
    List<AuditoriaUsuarioDto> usuarios();
}
