package com.cloud_technological.inversiones_prestar.services.implementations;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaListDto;
import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaUsuarioDto;
import com.cloud_technological.inversiones_prestar.entity.EventoAuditoriaEntity;
import com.cloud_technological.inversiones_prestar.repositories.auditoria.EventoAuditoriaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.auditoria.EventoAuditoriaQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.usuarios.UsuarioJPARepository;
import com.cloud_technological.inversiones_prestar.services.AuditoriaService;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auditoría general del sistema (HU-BE-020).
 *
 * <p>El registro de eventos es defensivo: cualquier fallo al auditar se
 * captura y se loguea, pero NO interrumpe la operación de negocio que lo
 * invocó. El evento se persiste en una transacción independiente
 * ({@code REQUIRES_NEW}) para que no quede sujeto a un rollback posterior
 * de la operación principal.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final EventoAuditoriaJPARepository auditoriaRepository;
    private final EventoAuditoriaQueryRepository auditoriaQueryRepository;
    private final UsuarioJPARepository usuarioRepository;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String tablaAfectada, Long registroId,
            Object valorAnterior, Object valorNuevo, String observacion) {
        try {
            Long usuarioId = securityUtils.getUsuarioId();
            String usuarioNombre = usuarioId == null ? null
                    : usuarioRepository.findById(usuarioId)
                            .map(u -> u.getNombre())
                            .orElse(null);

            auditoriaRepository.save(EventoAuditoriaEntity.builder()
                    .usuarioId(usuarioId)
                    .usuarioNombre(usuarioNombre)
                    .accion(accion == null ? null : accion.trim().toUpperCase())
                    .tablaAfectada(tablaAfectada)
                    .registroId(registroId)
                    .valorAnterior(aJson(valorAnterior))
                    .valorNuevo(aJson(valorNuevo))
                    .observacion(observacion)
                    .updatedBy(usuarioId)
                    .build());
        } catch (Exception e) {
            // El fallo de auditoría nunca debe romper la operación principal.
            log.warn("No se pudo registrar el evento de auditoría [{} / {} / {}]: {}",
                    accion, tablaAfectada, registroId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaListDto> listar(PageableDto<Object> pageable) {
        return auditoriaQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditoriaUsuarioDto> usuarios() {
        return auditoriaQueryRepository.usuariosConEventos();
    }

    /** Serializa el objeto a JSON; si es null o falla, devuelve null. */
    private String aJson(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (Exception e) {
            log.warn("No se pudo serializar el valor de auditoría a JSON: {}", e.getMessage());
            return String.valueOf(valor);
        }
    }
}
