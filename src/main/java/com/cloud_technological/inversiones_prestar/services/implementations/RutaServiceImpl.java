package com.cloud_technological.inversiones_prestar.services.implementations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.rutas.RutaComboDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaListDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaResponseDto;
import com.cloud_technological.inversiones_prestar.entity.RutaEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaTrabajadorEntity;
import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaTrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.services.RutaService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RutaServiceImpl implements RutaService {

    private final RutaJPARepository rutaRepository;
    private final RutaTrabajadorJPARepository rutaTrabajadorRepository;
    private final RutaQueryRepository rutaQueryRepository;
    private final TrabajadorJPARepository trabajadorRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public RutaResponseDto crear(RutaRequestDto dto) {
        String nombre = dto.getNombre().trim();

        rutaRepository.findByNombreAndDeletedAtIsNull(nombre).ifPresent(r -> {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe una ruta con el nombre " + nombre);
        });

        RutaEntity entity = RutaEntity.builder()
                .nombre(nombre)
                .zona(dto.getZona())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() == null ? Boolean.TRUE : dto.getActivo())
                .updatedBy(securityUtils.getUsuarioId())
                .build();

        RutaEntity guardada = rutaRepository.save(entity);

        if (dto.getTrabajadorId() != null) {
            reconciliarAsignacion(guardada.getId(), dto.getTrabajadorId());
        }

        return toResponse(guardada);
    }

    @Override
    @Transactional
    public RutaResponseDto actualizar(Long id, RutaRequestDto dto) {
        RutaEntity entity = buscar(id);
        String nombre = dto.getNombre().trim();

        rutaRepository.findByNombreAndDeletedAtIsNull(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "Ya existe otra ruta con el nombre " + nombre);
                });

        entity.setNombre(nombre);
        entity.setZona(dto.getZona());
        entity.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());
        rutaRepository.save(entity);

        reconciliarAsignacion(id, dto.getTrabajadorId());

        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RutaResponseDto obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RutaListDto> listar(PageableDto<Object> pageable) {
        return rutaQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RutaComboDto> listarActivas() {
        return rutaRepository.findByActivoTrueAndDeletedAtIsNullOrderByNombreAsc().stream()
                .map(r -> RutaComboDto.builder()
                        .id(r.getId())
                        .nombre(r.getNombre())
                        .zona(r.getZona())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public RutaResponseDto cambiarEstado(Long id, Boolean activo) {
        RutaEntity entity = buscar(id);
        entity.setActivo(activo);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());
        return toResponse(rutaRepository.save(entity));
    }

    @Override
    @Transactional
    public RutaResponseDto asignarTrabajador(Long id, Long trabajadorId) {
        RutaEntity entity = buscar(id);
        reconciliarAsignacion(id, trabajadorId);
        return toResponse(entity);
    }

    /* ===================== Asignación ===================== */

    /**
     * Ajusta la asignación activa de la ruta al trabajador indicado.
     * Cierra la asignación anterior (si la hay) y crea la nueva.
     * Si {@code nuevoTrabajadorId} es null, la ruta queda sin trabajador.
     */
    private void reconciliarAsignacion(Long rutaId, Long nuevoTrabajadorId) {
        Optional<RutaTrabajadorEntity> actualOpt = rutaTrabajadorRepository
                .findFirstByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(rutaId);

        Long actualId = actualOpt.map(RutaTrabajadorEntity::getTrabajadorId).orElse(null);
        if (Objects.equals(actualId, nuevoTrabajadorId)) {
            return;
        }

        Long usuarioId = securityUtils.getUsuarioId();

        actualOpt.ifPresent(actual -> {
            actual.setActivo(false);
            actual.setFechaFin(LocalDate.now());
            actual.setUpdatedAt(LocalDateTime.now());
            actual.setUpdatedBy(usuarioId);
            rutaTrabajadorRepository.save(actual);
        });

        if (nuevoTrabajadorId != null) {
            trabajadorRepository.findByIdAndDeletedAtIsNull(nuevoTrabajadorId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                            "El trabajador a asignar no existe"));

            RutaTrabajadorEntity nueva = RutaTrabajadorEntity.builder()
                    .rutaId(rutaId)
                    .trabajadorId(nuevoTrabajadorId)
                    .fechaInicio(LocalDate.now())
                    .activo(true)
                    .updatedBy(usuarioId)
                    .build();
            rutaTrabajadorRepository.save(nueva);
        }
    }

    /* ===================== Helpers ===================== */

    private RutaEntity buscar(Long id) {
        return rutaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));
    }

    private RutaResponseDto toResponse(RutaEntity entity) {
        Long trabajadorId = null;
        String trabajadorNombre = null;
        LocalDate fechaAsignacion = null;

        Optional<RutaTrabajadorEntity> asignacion = rutaTrabajadorRepository
                .findFirstByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(entity.getId());
        if (asignacion.isPresent()) {
            RutaTrabajadorEntity rt = asignacion.get();
            trabajadorId = rt.getTrabajadorId();
            fechaAsignacion = rt.getFechaInicio();
            trabajadorNombre = trabajadorRepository.findById(trabajadorId)
                    .map(TrabajadorEntity::getNombre)
                    .orElse(null);
        }

        return RutaResponseDto.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .zona(entity.getZona())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .trabajadorId(trabajadorId)
                .trabajadorNombre(trabajadorNombre)
                .fechaAsignacion(fechaAsignacion)
                .clientes(rutaQueryRepository.contarClientes(entity.getId()))
                .build();
    }
}
