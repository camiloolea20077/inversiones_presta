package com.cloud_technological.inversiones_prestar.services.implementations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteComboDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteListDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteRequestDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteResponseDto;
import com.cloud_technological.inversiones_prestar.entity.ClienteEntity;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteQueryRepository;
import com.cloud_technological.inversiones_prestar.services.ClienteService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    /** Estados que el administrador puede asignar manualmente. */
    private static final Set<String> ESTADOS_VALIDOS = Set.of("ACTIVO", "BLOQUEADO", "RETIRADO");

    private final ClienteJPARepository clienteRepository;
    private final ClienteQueryRepository clienteQueryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ClienteResponseDto crear(ClienteRequestDto dto) {
        String estado = dto.getEstado() == null || dto.getEstado().isBlank()
                ? "ACTIVO"
                : dto.getEstado().trim().toUpperCase();
        validarEstado(estado);

        ClienteEntity entity = ClienteEntity.builder()
                .documento(limpiar(dto.getDocumento()))
                .nombre(dto.getNombre().trim())
                .telefono(limpiar(dto.getTelefono()))
                .direccion(limpiar(dto.getDireccion()))
                .barrio(limpiar(dto.getBarrio()))
                .observacion(limpiar(dto.getObservacion()))
                .estado(estado)
                .activo(Boolean.TRUE)
                .updatedBy(securityUtils.getUsuarioId())
                .build();

        return toResponse(clienteRepository.save(entity));
    }

    @Override
    @Transactional
    public ClienteResponseDto actualizar(Long id, ClienteRequestDto dto) {
        ClienteEntity entity = buscar(id);

        entity.setDocumento(limpiar(dto.getDocumento()));
        entity.setNombre(dto.getNombre().trim());
        entity.setTelefono(limpiar(dto.getTelefono()));
        entity.setDireccion(limpiar(dto.getDireccion()));
        entity.setBarrio(limpiar(dto.getBarrio()));
        entity.setObservacion(limpiar(dto.getObservacion()));
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            String estado = dto.getEstado().trim().toUpperCase();
            validarEstado(estado);
            entity.setEstado(estado);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());

        return toResponse(clienteRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteListDto> listar(PageableDto<Object> pageable) {
        return clienteQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteComboDto> listarActivos() {
        return clienteRepository.findAll().stream()
                .filter(c -> c.getDeletedAt() == null && Boolean.TRUE.equals(c.getActivo())
                        && "ACTIVO".equals(c.getEstado()))
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(c -> ClienteComboDto.builder()
                        .id(c.getId())
                        .nombre(c.getNombre())
                        .documento(c.getDocumento())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ClienteResponseDto cambiarEstado(Long id, String estado) {
        String nuevo = estado == null ? "" : estado.trim().toUpperCase();
        validarEstado(nuevo);
        ClienteEntity entity = buscar(id);
        entity.setEstado(nuevo);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());
        return toResponse(clienteRepository.save(entity));
    }

    /* ===================== Helpers ===================== */

    private void validarEstado(String estado) {
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Estado de cliente no válido. Use ACTIVO, BLOQUEADO o RETIRADO");
        }
    }

    private String limpiar(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private ClienteEntity buscar(Long id) {
        return clienteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private ClienteResponseDto toResponse(ClienteEntity e) {
        return ClienteResponseDto.builder()
                .id(e.getId())
                .documento(e.getDocumento())
                .nombre(e.getNombre())
                .telefono(e.getTelefono())
                .direccion(e.getDireccion())
                .barrio(e.getBarrio())
                .observacion(e.getObservacion())
                .estado(e.getEstado())
                .activo(e.getActivo())
                .fechaRegistro(e.getCreatedAt() == null ? null : e.getCreatedAt().toLocalDate())
                .build();
    }
}
