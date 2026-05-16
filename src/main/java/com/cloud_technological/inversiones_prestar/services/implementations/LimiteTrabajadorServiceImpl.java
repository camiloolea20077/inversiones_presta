package com.cloud_technological.inversiones_prestar.services.implementations;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorResponseDto;
import com.cloud_technological.inversiones_prestar.entity.LimiteTrabajadorEntity;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.LimiteTrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.services.LimiteTrabajadorService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LimiteTrabajadorServiceImpl implements LimiteTrabajadorService {

    private final LimiteTrabajadorJPARepository limiteRepository;
    private final TrabajadorJPARepository trabajadorRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public LimiteTrabajadorResponseDto obtenerPorTrabajador(Long trabajadorId) {
        validarTrabajador(trabajadorId);
        return limiteRepository.findByTrabajadorIdAndActivoTrueAndDeletedAtIsNull(trabajadorId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public LimiteTrabajadorResponseDto guardar(Long trabajadorId, LimiteTrabajadorRequestDto dto) {
        validarTrabajador(trabajadorId);

        if (dto.getTasaMaxima().compareTo(dto.getTasaMinima()) < 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La tasa máxima no puede ser menor que la tasa mínima");
        }

        Long usuarioId = securityUtils.getUsuarioId();

        // Cierra la configuración activa anterior para conservar el historial.
        limiteRepository.findByTrabajadorIdAndActivoTrueAndDeletedAtIsNull(trabajadorId)
                .ifPresent(actual -> {
                    actual.setActivo(false);
                    actual.setUpdatedAt(LocalDateTime.now());
                    actual.setUpdatedBy(usuarioId);
                    limiteRepository.save(actual);
                });

        LimiteTrabajadorEntity entity = LimiteTrabajadorEntity.builder()
                .trabajadorId(trabajadorId)
                .montoMaximoPrestamo(dto.getMontoMaximoPrestamo())
                .tasaMinima(dto.getTasaMinima())
                .tasaMaxima(dto.getTasaMaxima())
                .plazoMaximoDias(dto.getPlazoMaximoDias())
                .puedeCrearCliente(dto.getPuedeCrearCliente() == null ? Boolean.TRUE : dto.getPuedeCrearCliente())
                .puedeCrearPrestamo(dto.getPuedeCrearPrestamo() == null ? Boolean.TRUE : dto.getPuedeCrearPrestamo())
                .puedeDefinirTasa(dto.getPuedeDefinirTasa() == null ? Boolean.TRUE : dto.getPuedeDefinirTasa())
                .activo(true)
                .updatedBy(usuarioId)
                .build();

        return toResponse(limiteRepository.save(entity));
    }

    private void validarTrabajador(Long trabajadorId) {
        trabajadorRepository.findByIdAndDeletedAtIsNull(trabajadorId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Trabajador no encontrado"));
    }

    private LimiteTrabajadorResponseDto toResponse(LimiteTrabajadorEntity entity) {
        return LimiteTrabajadorResponseDto.builder()
                .id(entity.getId())
                .trabajadorId(entity.getTrabajadorId())
                .montoMaximoPrestamo(entity.getMontoMaximoPrestamo())
                .tasaMinima(entity.getTasaMinima())
                .tasaMaxima(entity.getTasaMaxima())
                .plazoMaximoDias(entity.getPlazoMaximoDias())
                .puedeCrearCliente(entity.getPuedeCrearCliente())
                .puedeCrearPrestamo(entity.getPuedeCrearPrestamo())
                .puedeDefinirTasa(entity.getPuedeDefinirTasa())
                .activo(entity.getActivo())
                .build();
    }
}
