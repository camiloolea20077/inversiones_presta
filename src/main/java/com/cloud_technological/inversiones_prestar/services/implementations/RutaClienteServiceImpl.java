package com.cloud_technological.inversiones_prestar.services.implementations;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.clientes.RutaClienteListDto;
import com.cloud_technological.inversiones_prestar.entity.RutaClienteEntity;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.RutaClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaJPARepository;
import com.cloud_technological.inversiones_prestar.services.RutaClienteService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RutaClienteServiceImpl implements RutaClienteService {

    private static final int DESPLAZAMIENTO_TEMP = 100000;

    private final RutaClienteJPARepository rutaClienteRepository;
    private final ClienteQueryRepository clienteQueryRepository;
    private final RutaJPARepository rutaRepository;
    private final ClienteJPARepository clienteRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<RutaClienteListDto> listarPorRuta(Long rutaId) {
        validarRuta(rutaId);
        return clienteQueryRepository.listarClientesDeRuta(rutaId);
    }

    @Override
    @Transactional
    public List<RutaClienteListDto> insertarEnRuta(Long rutaId, Long clienteId, Integer ordenBase) {
        validarRuta(rutaId);

        clienteRepository.findByIdAndDeletedAtIsNull(clienteId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El cliente a insertar no existe"));

        rutaClienteRepository.findByRutaIdAndClienteIdAndActivoTrueAndDeletedAtIsNull(rutaId, clienteId)
                .ifPresent(rc -> {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "El cliente ya está activo en esta ruta");
                });

        clienteQueryRepository.insertarClienteEnRuta(
                rutaId, clienteId, ordenBase, securityUtils.getUsuarioId());

        return clienteQueryRepository.listarClientesDeRuta(rutaId);
    }

    @Override
    @Transactional
    public List<RutaClienteListDto> reordenar(Long rutaId, List<Long> ordenIds) {
        validarRuta(rutaId);

        List<RutaClienteEntity> actuales = rutaClienteRepository
                .findByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByOrdenAsc(rutaId);

        if (ordenIds.size() != actuales.size()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El orden recibido no coincide con los clientes de la ruta");
        }

        Map<Long, RutaClienteEntity> porId = new LinkedHashMap<>();
        actuales.forEach(rc -> porId.put(rc.getId(), rc));

        for (Long id : ordenIds) {
            if (!porId.containsKey(id)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El orden contiene un cliente que no pertenece a la ruta");
            }
        }

        Long usuarioId = securityUtils.getUsuarioId();

        // Fase 1: desplazar a un rango temporal para no chocar con el índice único.
        actuales.forEach(rc -> rc.setOrden(rc.getOrden() + DESPLAZAMIENTO_TEMP));
        rutaClienteRepository.saveAllAndFlush(actuales);

        // Fase 2: asignar el orden definitivo 1..N.
        for (int i = 0; i < ordenIds.size(); i++) {
            RutaClienteEntity rc = porId.get(ordenIds.get(i));
            rc.setOrden(i + 1);
            rc.setUpdatedAt(LocalDateTime.now());
            rc.setUpdatedBy(usuarioId);
        }
        rutaClienteRepository.saveAllAndFlush(actuales);

        return clienteQueryRepository.listarClientesDeRuta(rutaId);
    }

    @Override
    @Transactional
    public List<RutaClienteListDto> quitarDeRuta(Long rutaClienteId) {
        RutaClienteEntity rc = rutaClienteRepository.findByIdAndDeletedAtIsNull(rutaClienteId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El cliente no está en la ruta"));

        rc.setActivo(false);
        rc.setUpdatedAt(LocalDateTime.now());
        rc.setUpdatedBy(securityUtils.getUsuarioId());
        rutaClienteRepository.save(rc);

        return clienteQueryRepository.listarClientesDeRuta(rc.getRutaId());
    }

    private void validarRuta(Long rutaId) {
        rutaRepository.findByIdAndDeletedAtIsNull(rutaId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));
    }
}
