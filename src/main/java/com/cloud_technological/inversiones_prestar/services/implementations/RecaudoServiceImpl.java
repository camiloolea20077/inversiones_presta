package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.recaudos.RecaudoDetalleDto;
import com.cloud_technological.inversiones_prestar.dto.recaudos.RecaudoDiarioResponseDto;
import com.cloud_technological.inversiones_prestar.entity.ClienteEntity;
import com.cloud_technological.inversiones_prestar.entity.PrestamoEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDetalleEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDiarioEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaClienteEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaTrabajadorEntity;
import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.RutaClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.PrestamoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDetalleJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDiarioJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaTrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.services.RecaudoService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecaudoServiceImpl implements RecaudoService {

    private final RecaudoDiarioJPARepository recaudoRepository;
    private final RecaudoDetalleJPARepository detalleRepository;
    private final RutaJPARepository rutaRepository;
    private final RutaClienteJPARepository rutaClienteRepository;
    private final RutaTrabajadorJPARepository rutaTrabajadorRepository;
    private final TrabajadorJPARepository trabajadorRepository;
    private final ClienteJPARepository clienteRepository;
    private final PrestamoJPARepository prestamoRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public RecaudoDiarioResponseDto generar(Long rutaId, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();

        RutaEntity ruta = rutaRepository.findByIdAndDeletedAtIsNull(rutaId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        RutaTrabajadorEntity asignacion = rutaTrabajadorRepository
                .findFirstByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(rutaId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "La ruta no tiene un trabajador asignado"));

        Long usuarioId = securityUtils.getUsuarioId();

        List<RutaClienteEntity> rutaClientes = rutaClienteRepository
                .findByRutaIdAndActivoTrueAndDeletedAtIsNullOrderByOrdenAsc(rutaId);

        var existente = recaudoRepository
                .findFirstByRutaIdAndFechaRecaudoAndEstadoNotAndDeletedAtIsNull(rutaId, dia, "ANULADO");

        // Si ya existe el recaudo del día, se sincroniza con la ruta actual.
        if (existente.isPresent()) {
            sincronizar(existente.get(), rutaClientes, usuarioId);
            return construirResponse(existente.get(), ruta);
        }

        // Genera la planilla nueva.
        RecaudoDiarioEntity recaudo = recaudoRepository.save(RecaudoDiarioEntity.builder()
                .rutaId(rutaId)
                .trabajadorId(asignacion.getTrabajadorId())
                .fechaRecaudo(dia)
                .totalEsperado(BigDecimal.ZERO)
                .totalRecaudado(BigDecimal.ZERO)
                .estado("ABIERTO")
                .updatedBy(usuarioId)
                .build());

        List<RecaudoDetalleEntity> detalles = new ArrayList<>();
        for (RutaClienteEntity rc : rutaClientes) {
            detalles.add(construirDetalle(recaudo.getId(), rc.getClienteId(), rc.getOrden(), usuarioId));
        }
        detalleRepository.saveAll(detalles);

        recaudo.setTotalEsperado(sumarEsperado(detalles));
        recaudoRepository.save(recaudo);

        return construirResponse(recaudo, ruta);
    }

    @Override
    @Transactional(readOnly = true)
    public RecaudoDiarioResponseDto obtener(Long id) {
        RecaudoDiarioEntity recaudo = recaudoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Recaudo no encontrado"));
        RutaEntity ruta = rutaRepository.findById(recaudo.getRutaId()).orElse(null);
        return construirResponse(recaudo, ruta);
    }

    @Override
    @Transactional
    public RecaudoDiarioResponseDto miRutaDiaria(LocalDate fecha) {
        Long usuarioId = securityUtils.getUsuarioId();
        if (usuarioId == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado");
        }

        TrabajadorEntity trabajador = trabajadorRepository.findByUsuarioIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El usuario no está asociado a un trabajador"));

        RutaTrabajadorEntity asignacion = rutaTrabajadorRepository
                .findFirstByTrabajadorIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(
                        trabajador.getId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "No tienes una ruta asignada"));

        return generar(asignacion.getRutaId(), fecha);
    }

    /* ===================== Sincronización ===================== */

    /**
     * Mantiene el recaudo al día con la ruta: agrega los clientes que se
     * incorporaron al recorrido y vincula préstamos creados después.
     */
    private void sincronizar(RecaudoDiarioEntity recaudo, List<RutaClienteEntity> rutaClientes,
            Long usuarioId) {
        if (!"ABIERTO".equals(recaudo.getEstado()) && !"EN_PROCESO".equals(recaudo.getEstado())) {
            return;
        }

        List<RecaudoDetalleEntity> detalles = detalleRepository
                .findByRecaudoDiarioIdAndDeletedAtIsNullOrderByOrdenAsc(recaudo.getId());

        Set<Long> clientesConDetalle = detalles.stream()
                .map(RecaudoDetalleEntity::getClienteId)
                .collect(Collectors.toCollection(HashSet::new));

        int siguienteOrden = detalles.stream()
                .mapToInt(RecaudoDetalleEntity::getOrden).max().orElse(0) + 1;

        boolean cambios = false;
        List<RecaudoDetalleEntity> nuevos = new ArrayList<>();

        // Clientes nuevos en el recorrido.
        for (RutaClienteEntity rc : rutaClientes) {
            if (!clientesConDetalle.contains(rc.getClienteId())) {
                nuevos.add(construirDetalle(recaudo.getId(), rc.getClienteId(),
                        siguienteOrden++, usuarioId));
                cambios = true;
            }
        }

        // Préstamos creados después de generar la planilla.
        for (RecaudoDetalleEntity d : detalles) {
            if (d.getPrestamoId() == null) {
                PrestamoEntity prestamo = prestamoActivo(d.getClienteId());
                if (prestamo != null) {
                    d.setPrestamoId(prestamo.getId());
                    d.setValorEsperado(prestamo.getCuotaDiaria());
                    d.setSaldoPendiente(prestamo.getSaldoActual());
                    if ("SIN_PRESTAMO".equals(d.getEstado())) {
                        d.setEstado("PENDIENTE");
                    }
                    cambios = true;
                }
            }
        }

        if (cambios) {
            if (!nuevos.isEmpty()) {
                detalleRepository.saveAll(nuevos);
            }
            detalleRepository.saveAll(detalles);
            List<RecaudoDetalleEntity> todos = new ArrayList<>(detalles);
            todos.addAll(nuevos);
            recaudo.setTotalEsperado(sumarEsperado(todos));
            recaudoRepository.save(recaudo);
        }
    }

    /* ===================== Helpers ===================== */

    private PrestamoEntity prestamoActivo(Long clienteId) {
        return prestamoRepository
                .findFirstByClienteIdAndEstadoAndDeletedAtIsNullOrderByCreatedAtDesc(clienteId, "ACTIVO")
                .orElse(null);
    }

    private RecaudoDetalleEntity construirDetalle(Long recaudoId, Long clienteId, int orden,
            Long usuarioId) {
        PrestamoEntity prestamo = prestamoActivo(clienteId);
        BigDecimal esperado = prestamo != null ? prestamo.getCuotaDiaria() : BigDecimal.ZERO;
        BigDecimal saldo = prestamo != null ? prestamo.getSaldoActual() : BigDecimal.ZERO;
        return RecaudoDetalleEntity.builder()
                .recaudoDiarioId(recaudoId)
                .clienteId(clienteId)
                .prestamoId(prestamo != null ? prestamo.getId() : null)
                .orden(orden)
                .valorEsperado(esperado)
                .valorPagado(BigDecimal.ZERO)
                .saldoPendiente(saldo)
                .estado(prestamo != null ? "PENDIENTE" : "SIN_PRESTAMO")
                .updatedBy(usuarioId)
                .build();
    }

    private BigDecimal sumarEsperado(List<RecaudoDetalleEntity> detalles) {
        return detalles.stream()
                .map(RecaudoDetalleEntity::getValorEsperado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private RecaudoDiarioResponseDto construirResponse(RecaudoDiarioEntity recaudo, RutaEntity ruta) {
        List<RecaudoDetalleEntity> detalles = detalleRepository
                .findByRecaudoDiarioIdAndDeletedAtIsNullOrderByOrdenAsc(recaudo.getId());

        List<Long> clienteIds = detalles.stream().map(RecaudoDetalleEntity::getClienteId).toList();
        Map<Long, ClienteEntity> clientes = clienteRepository.findAllById(clienteIds).stream()
                .collect(Collectors.toMap(ClienteEntity::getId, Function.identity()));

        String trabajadorNombre = trabajadorRepository.findById(recaudo.getTrabajadorId())
                .map(TrabajadorEntity::getNombre)
                .orElse(null);

        List<RecaudoDetalleDto> detalleDtos = new ArrayList<>();
        int pagados = 0, pendientes = 0, parciales = 0, noPago = 0;

        for (RecaudoDetalleEntity d : detalles) {
            ClienteEntity c = clientes.get(d.getClienteId());
            detalleDtos.add(RecaudoDetalleDto.builder()
                    .id(d.getId())
                    .orden(d.getOrden())
                    .clienteId(d.getClienteId())
                    .clienteNombre(c != null ? c.getNombre() : null)
                    .clienteTelefono(c != null ? c.getTelefono() : null)
                    .clienteDireccion(c != null ? c.getDireccion() : null)
                    .prestamoId(d.getPrestamoId())
                    .valorEsperado(d.getValorEsperado())
                    .valorPagado(d.getValorPagado())
                    .saldoPendiente(d.getSaldoPendiente())
                    .estado(d.getEstado())
                    .observacion(d.getObservacion())
                    .build());

            switch (d.getEstado()) {
                case "PAGADO" -> pagados++;
                case "PARCIAL" -> parciales++;
                case "NO_PAGO" -> noPago++;
                default -> pendientes++;
            }
        }

        return RecaudoDiarioResponseDto.builder()
                .id(recaudo.getId())
                .rutaId(recaudo.getRutaId())
                .rutaNombre(ruta != null ? ruta.getNombre() : null)
                .trabajadorId(recaudo.getTrabajadorId())
                .trabajadorNombre(trabajadorNombre)
                .fechaRecaudo(recaudo.getFechaRecaudo())
                .totalEsperado(recaudo.getTotalEsperado())
                .totalRecaudado(recaudo.getTotalRecaudado())
                .estado(recaudo.getEstado())
                .totalClientes(detalles.size())
                .clientesPagados(pagados)
                .clientesPendientes(pendientes)
                .clientesParciales(parciales)
                .clientesNoPago(noPago)
                .detalles(detalleDtos)
                .build();
    }
}
