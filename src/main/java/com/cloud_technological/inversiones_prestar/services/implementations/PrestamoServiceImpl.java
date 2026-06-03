package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.CuotaPrestamoDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoListDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionResponseDto;
import com.cloud_technological.inversiones_prestar.entity.ClienteEntity;
import com.cloud_technological.inversiones_prestar.entity.CuotaPrestamoEntity;
import com.cloud_technological.inversiones_prestar.entity.LimiteTrabajadorEntity;
import com.cloud_technological.inversiones_prestar.entity.PrestamoEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDetalleEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDiarioEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaClienteEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaEntity;
import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.clientes.RutaClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.CuotaPrestamoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.PrestamoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.PrestamoQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDetalleJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDiarioJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.LimiteTrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.services.AuditoriaService;
import com.cloud_technological.inversiones_prestar.services.CajaService;
import com.cloud_technological.inversiones_prestar.services.PresupuestoService;
import com.cloud_technological.inversiones_prestar.services.PrestamoService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {

    private static final Set<String> TIPOS_INTERES = Set.of("FIJO_PRESTAMO", "MENSUAL", "DIARIO");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal DIAS_MES = BigDecimal.valueOf(30);

    private final PrestamoJPARepository prestamoRepository;
    private final CuotaPrestamoJPARepository cuotaRepository;
    private final PrestamoQueryRepository prestamoQueryRepository;
    private final ClienteJPARepository clienteRepository;
    private final ClienteQueryRepository clienteQueryRepository;
    private final RutaClienteJPARepository rutaClienteRepository;
    private final RutaJPARepository rutaRepository;
    private final TrabajadorJPARepository trabajadorRepository;
    private final LimiteTrabajadorJPARepository limiteRepository;
    private final RecaudoDiarioJPARepository recaudoRepository;
    private final RecaudoDetalleJPARepository recaudoDetalleRepository;
    private final CajaService cajaService;
    private final PresupuestoService presupuestoService;
    private final AuditoriaService auditoriaService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public PrestamoResponseDto crear(PrestamoRequestDto dto) {
        ClienteEntity cliente = clienteRepository.findByIdAndDeletedAtIsNull(dto.getClienteId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "El cliente no existe"));
        RutaEntity ruta = rutaRepository.findByIdAndDeletedAtIsNull(dto.getRutaId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "La ruta no existe"));
        TrabajadorEntity trabajador = trabajadorRepository.findByIdAndDeletedAtIsNull(dto.getTrabajadorId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "El trabajador no existe"));

        String tipoInteres = normalizarTipo(dto.getTipoInteres());

        validarLimites(trabajador.getId(), dto.getMontoPrestado(), dto.getTasaPorcentaje(),
                dto.getPlazoDias());

        SimulacionResponseDto calculo = calcular(
                dto.getMontoPrestado(), dto.getTasaPorcentaje(), dto.getPlazoDias(), tipoInteres);

        // Verifica que el presupuesto alcance: se compromete el total a pagar
        // (capital + intereses), no solo el capital (interés sobre interés).
        presupuestoService.validarYDescontarPrestamo(calculo.getTotalPagar());

        LocalDate inicio = LocalDate.now();
        Long usuarioId = securityUtils.getUsuarioId();

        PrestamoEntity prestamo = PrestamoEntity.builder()
                .clienteId(cliente.getId())
                .rutaId(ruta.getId())
                .trabajadorId(trabajador.getId())
                .montoPrestado(escala(dto.getMontoPrestado()))
                .tasaPorcentaje(dto.getTasaPorcentaje())
                .tipoInteres(tipoInteres)
                .plazoDias(dto.getPlazoDias())
                .valorInteres(calculo.getValorInteres())
                .totalPagar(calculo.getTotalPagar())
                .cuotaDiaria(calculo.getCuotaDiaria())
                .saldoActual(calculo.getTotalPagar())
                .fechaInicio(inicio)
                .fechaFin(inicio.plusDays(dto.getPlazoDias()))
                .estado("ACTIVO")
                .observacion(dto.getObservacion())
                .updatedBy(usuarioId)
                .build();

        PrestamoEntity guardado = prestamoRepository.save(prestamo);

        List<CuotaPrestamoEntity> cuotas = generarCuotas(guardado, calculo, inicio, usuarioId);
        cuotaRepository.saveAll(cuotas);

        // El cliente del préstamo queda en el recorrido de la ruta seleccionada.
        asegurarClienteEnRuta(ruta.getId(), cliente.getId(), usuarioId);

        // Movimiento de caja PRESTAMO_ENTREGADO si el trabajador tiene caja abierta.
        cajaService.registrarPrestamoEntregado(trabajador.getId(), ruta.getId(), inicio,
                guardado.getMontoPrestado(), guardado.getId(), usuarioId);

        // Auditoría (HU-BE-020): creación de préstamo.
        auditoriaService.registrar("CREAR", "prestamos", guardado.getId(), null, guardado,
                "Préstamo creado para el cliente " + cliente.getNombre()
                        + " por " + guardado.getMontoPrestado());

        return toResponse(guardado, cuotas, cliente, ruta, trabajador);
    }

    @Override
    @Transactional
    public ClienteConPrestamoResponseDto crearClienteConPrestamo(ClienteConPrestamoRequestDto dto) {
        // 1. Validaciones previas (todo se valida antes de escribir nada).
        RutaEntity ruta = rutaRepository.findByIdAndDeletedAtIsNull(dto.getRutaId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "La ruta no existe"));
        TrabajadorEntity trabajador = trabajadorRepository.findByIdAndDeletedAtIsNull(dto.getTrabajadorId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "El trabajador no existe"));

        String tipoInteres = normalizarTipo(dto.getTipoInteres());
        validarLimites(trabajador.getId(), dto.getMontoPrestado(), dto.getTasaPorcentaje(),
                dto.getPlazoDias());

        SimulacionResponseDto calculo = calcular(
                dto.getMontoPrestado(), dto.getTasaPorcentaje(), dto.getPlazoDias(), tipoInteres);

        // Verifica que el presupuesto alcance: se compromete el total a pagar
        // (capital + intereses), no solo el capital (interés sobre interés).
        presupuestoService.validarYDescontarPrestamo(calculo.getTotalPagar());

        Long usuarioId = securityUtils.getUsuarioId();

        // 2. Crear el cliente.
        ClienteEntity cliente = clienteRepository.save(ClienteEntity.builder()
                .documento(limpiar(dto.getDocumento()))
                .nombre(dto.getNombre().trim())
                .telefono(limpiar(dto.getTelefono()))
                .direccion(limpiar(dto.getDireccion()))
                .barrio(limpiar(dto.getBarrio()))
                .observacion(limpiar(dto.getObservacionCliente()))
                .estado("ACTIVO")
                .activo(Boolean.TRUE)
                .updatedBy(usuarioId)
                .build());

        // 3. Insertarlo en la ruta después del cliente base.
        clienteQueryRepository.insertarClienteEnRuta(
                ruta.getId(), cliente.getId(), dto.getOrdenBase(), usuarioId);

        Integer ordenAsignado = rutaClienteRepository
                .findByRutaIdAndClienteIdAndActivoTrueAndDeletedAtIsNull(ruta.getId(), cliente.getId())
                .map(RutaClienteEntity::getOrden)
                .orElse(null);

        // 4. Crear el préstamo activo + 5. generar las cuotas (reusa `calculo`).
        LocalDate inicio = LocalDate.now();
        PrestamoEntity prestamo = prestamoRepository.save(PrestamoEntity.builder()
                .clienteId(cliente.getId())
                .rutaId(ruta.getId())
                .trabajadorId(trabajador.getId())
                .montoPrestado(escala(dto.getMontoPrestado()))
                .tasaPorcentaje(dto.getTasaPorcentaje())
                .tipoInteres(tipoInteres)
                .plazoDias(dto.getPlazoDias())
                .valorInteres(calculo.getValorInteres())
                .totalPagar(calculo.getTotalPagar())
                .cuotaDiaria(calculo.getCuotaDiaria())
                .saldoActual(calculo.getTotalPagar())
                .fechaInicio(inicio)
                .fechaFin(inicio.plusDays(dto.getPlazoDias()))
                .estado("ACTIVO")
                .observacion(dto.getObservacionPrestamo())
                .updatedBy(usuarioId)
                .build());

        List<CuotaPrestamoEntity> cuotas = generarCuotas(prestamo, calculo, inicio, usuarioId);
        cuotaRepository.saveAll(cuotas);

        // 6. Actualizar la planilla diaria si ya existe.
        Long recaudoId = actualizarPlanillaDiaria(
                ruta.getId(), cliente.getId(), ordenAsignado, prestamo, usuarioId);

        // 7. Movimiento de caja PRESTAMO_ENTREGADO (HU-BE-015 / HU-BE-016).
        // Suma a valor_prestamos_entregados de la caja abierta del trabajador.
        cajaService.registrarPrestamoEntregado(trabajador.getId(), ruta.getId(), inicio,
                prestamo.getMontoPrestado(), prestamo.getId(), usuarioId);

        // Auditoría (HU-BE-020): creación de cliente y préstamo en una sola operación.
        auditoriaService.registrar("CREAR", "clientes", cliente.getId(), null, cliente,
                "Creación de cliente: " + cliente.getNombre());
        auditoriaService.registrar("CREAR", "prestamos", prestamo.getId(), null, prestamo,
                "Préstamo creado para el cliente " + cliente.getNombre()
                        + " por " + prestamo.getMontoPrestado());

        return ClienteConPrestamoResponseDto.builder()
                .clienteId(cliente.getId())
                .clienteNombre(cliente.getNombre())
                .ordenAsignado(ordenAsignado)
                .prestamo(toResponse(prestamo, cuotas, cliente, ruta, trabajador))
                .recaudoDiarioId(recaudoId)
                .planillaActualizada(recaudoId != null)
                .build();
    }

    /**
     * Si existe planilla diaria abierta para la ruta, agrega el renglón del
     * nuevo cliente con su préstamo recién creado y recalcula el total esperado.
     *
     * @return el id del recaudo actualizado, o null si no había planilla del día.
     */
    private Long actualizarPlanillaDiaria(Long rutaId, Long clienteId, Integer orden,
            PrestamoEntity prestamo, Long usuarioId) {
        RecaudoDiarioEntity recaudo = recaudoRepository
                .findFirstByRutaIdAndFechaRecaudoAndEstadoNotAndDeletedAtIsNull(
                        rutaId, LocalDate.now(), "ANULADO")
                .orElse(null);
        if (recaudo == null) {
            return null;
        }
        if (!"ABIERTO".equals(recaudo.getEstado()) && !"EN_PROCESO".equals(recaudo.getEstado())) {
            return recaudo.getId();
        }

        List<RecaudoDetalleEntity> detalles = recaudoDetalleRepository
                .findByRecaudoDiarioIdAndDeletedAtIsNullOrderByOrdenAsc(recaudo.getId());

        boolean yaTieneDetalle = detalles.stream()
                .anyMatch(d -> d.getClienteId().equals(clienteId));
        if (!yaTieneDetalle) {
            int siguienteOrden = detalles.stream()
                    .mapToInt(RecaudoDetalleEntity::getOrden).max().orElse(0) + 1;
            int ordenDestino = orden != null ? orden : siguienteOrden;

            // Hace hueco en la planilla: desplaza una posición los renglones con
            // orden >= ordenDestino. Se usa un rango temporal para no chocar con
            // el índice único (recaudo_diario_id, orden).
            List<RecaudoDetalleEntity> aDesplazar = detalles.stream()
                    .filter(d -> d.getOrden() >= ordenDestino)
                    .toList();
            if (!aDesplazar.isEmpty()) {
                aDesplazar.forEach(d -> d.setOrden(d.getOrden() + 100000));
                recaudoDetalleRepository.saveAllAndFlush(aDesplazar);
                aDesplazar.forEach(d -> {
                    d.setOrden(d.getOrden() - 100000 + 1);
                    d.setUpdatedAt(LocalDateTime.now());
                    d.setUpdatedBy(usuarioId);
                });
                recaudoDetalleRepository.saveAllAndFlush(aDesplazar);
            }

            RecaudoDetalleEntity detalle = recaudoDetalleRepository.save(RecaudoDetalleEntity.builder()
                    .recaudoDiarioId(recaudo.getId())
                    .clienteId(clienteId)
                    .prestamoId(prestamo.getId())
                    .orden(ordenDestino)
                    .valorEsperado(prestamo.getCuotaDiaria())
                    .valorPagado(BigDecimal.ZERO)
                    .saldoPendiente(prestamo.getSaldoActual())
                    .estado("PENDIENTE")
                    .updatedBy(usuarioId)
                    .build());
            recaudo.setTotalEsperado(recaudo.getTotalEsperado().add(detalle.getValorEsperado()));
            recaudo.setUpdatedAt(LocalDateTime.now());
            recaudo.setUpdatedBy(usuarioId);
            recaudoRepository.save(recaudo);
        }
        return recaudo.getId();
    }

    private String limpiar(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    /** Agrega el cliente al recorrido de la ruta si aún no está en ella. */
    private void asegurarClienteEnRuta(Long rutaId, Long clienteId, Long usuarioId) {
        boolean yaEnRuta = rutaClienteRepository
                .findByRutaIdAndClienteIdAndActivoTrueAndDeletedAtIsNull(rutaId, clienteId)
                .isPresent();
        if (!yaEnRuta) {
            clienteQueryRepository.insertarClienteEnRuta(rutaId, clienteId, null, usuarioId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoResponseDto obtener(Long id) {
        PrestamoEntity prestamo = prestamoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));

        List<CuotaPrestamoEntity> cuotas = cuotaRepository
                .findByPrestamoIdAndDeletedAtIsNullOrderByNumeroCuotaAsc(id);

        ClienteEntity cliente = clienteRepository.findById(prestamo.getClienteId()).orElse(null);
        RutaEntity ruta = rutaRepository.findById(prestamo.getRutaId()).orElse(null);
        TrabajadorEntity trabajador = trabajadorRepository.findById(prestamo.getTrabajadorId()).orElse(null);

        return toResponse(prestamo, cuotas, cliente, ruta, trabajador);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrestamoListDto> listar(PageableDto<Object> pageable) {
        return prestamoQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulacionResponseDto simular(SimulacionRequestDto dto) {
        return calcular(dto.getMontoPrestado(), dto.getTasaPorcentaje(),
                dto.getPlazoDias(), normalizarTipo(dto.getTipoInteres()));
    }

    /* ===================== Cálculo ===================== */

    private SimulacionResponseDto calcular(BigDecimal monto, BigDecimal tasa, int plazo, String tipo) {
        BigDecimal tasaFraccion = tasa.divide(CIEN, 10, RoundingMode.HALF_UP);
        BigDecimal valorInteres;
        switch (tipo) {
            case "DIARIO" -> valorInteres = monto.multiply(tasaFraccion)
                    .multiply(BigDecimal.valueOf(plazo));
            case "MENSUAL" -> valorInteres = monto.multiply(tasaFraccion)
                    .multiply(BigDecimal.valueOf(plazo))
                    .divide(DIAS_MES, 10, RoundingMode.HALF_UP);
            default -> valorInteres = monto.multiply(tasaFraccion); // FIJO_PRESTAMO
        }
        valorInteres = escala(valorInteres);
        BigDecimal total = escala(monto.add(valorInteres));
        BigDecimal cuota = total.divide(BigDecimal.valueOf(plazo), 2, RoundingMode.HALF_UP);

        return SimulacionResponseDto.builder()
                .montoPrestado(escala(monto))
                .valorInteres(valorInteres)
                .totalPagar(total)
                .cuotaDiaria(cuota)
                .plazoDias(plazo)
                .build();
    }

    private List<CuotaPrestamoEntity> generarCuotas(PrestamoEntity prestamo,
            SimulacionResponseDto calculo, LocalDate inicio, Long usuarioId) {
        int plazo = prestamo.getPlazoDias();
        BigDecimal cuota = calculo.getCuotaDiaria();
        BigDecimal acumulado = BigDecimal.ZERO;
        List<CuotaPrestamoEntity> cuotas = new ArrayList<>();

        for (int numero = 1; numero <= plazo; numero++) {
            BigDecimal valor;
            if (numero < plazo) {
                valor = cuota;
                acumulado = acumulado.add(cuota);
            } else {
                // La última cuota ajusta la diferencia por redondeo.
                valor = escala(calculo.getTotalPagar().subtract(acumulado));
            }
            cuotas.add(CuotaPrestamoEntity.builder()
                    .prestamoId(prestamo.getId())
                    .numeroCuota(numero)
                    .fechaCuota(inicio.plusDays(numero))
                    .valorCuota(valor)
                    .valorPagado(BigDecimal.ZERO)
                    .saldoCuota(valor)
                    .estado("PENDIENTE")
                    .updatedBy(usuarioId)
                    .build());
        }
        return cuotas;
    }

    /* ===================== Validación de límites ===================== */

    private void validarLimites(Long trabajadorId, BigDecimal monto, BigDecimal tasa, int plazo) {
        LimiteTrabajadorEntity limite = limiteRepository
                .findByTrabajadorIdAndActivoTrueAndDeletedAtIsNull(trabajadorId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El trabajador no tiene límites configurados, no puede crear préstamos"));

        if (Boolean.FALSE.equals(limite.getPuedeCrearPrestamo())) {
            throw new GlobalException(HttpStatus.FORBIDDEN,
                    "El trabajador no tiene permiso para crear préstamos");
        }
        if (monto.compareTo(limite.getMontoMaximoPrestamo()) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El monto supera el máximo permitido para el trabajador ("
                            + limite.getMontoMaximoPrestamo() + ")");
        }
        if (tasa.compareTo(limite.getTasaMinima()) < 0 || tasa.compareTo(limite.getTasaMaxima()) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La tasa debe estar entre " + limite.getTasaMinima()
                            + "% y " + limite.getTasaMaxima() + "%");
        }
        if (plazo > limite.getPlazoMaximoDias()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El plazo supera el máximo permitido para el trabajador ("
                            + limite.getPlazoMaximoDias() + " días)");
        }
    }

    /* ===================== Helpers ===================== */

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "FIJO_PRESTAMO";
        }
        String t = tipo.trim().toUpperCase();
        if (!TIPOS_INTERES.contains(t)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Tipo de interés no válido. Use FIJO_PRESTAMO, MENSUAL o DIARIO");
        }
        return t;
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private PrestamoResponseDto toResponse(PrestamoEntity p, List<CuotaPrestamoEntity> cuotas,
            ClienteEntity cliente, RutaEntity ruta, TrabajadorEntity trabajador) {
        List<CuotaPrestamoDto> cuotasDto = cuotas.stream()
                .map(c -> CuotaPrestamoDto.builder()
                        .id(c.getId())
                        .numeroCuota(c.getNumeroCuota())
                        .fechaCuota(c.getFechaCuota())
                        .valorCuota(c.getValorCuota())
                        .valorPagado(c.getValorPagado())
                        .saldoCuota(c.getSaldoCuota())
                        .estado(c.getEstado())
                        .build())
                .toList();

        return PrestamoResponseDto.builder()
                .id(p.getId())
                .clienteId(p.getClienteId())
                .clienteNombre(cliente == null ? null : cliente.getNombre())
                .rutaId(p.getRutaId())
                .rutaNombre(ruta == null ? null : ruta.getNombre())
                .trabajadorId(p.getTrabajadorId())
                .trabajadorNombre(trabajador == null ? null : trabajador.getNombre())
                .montoPrestado(p.getMontoPrestado())
                .tasaPorcentaje(p.getTasaPorcentaje())
                .tipoInteres(p.getTipoInteres())
                .plazoDias(p.getPlazoDias())
                .valorInteres(p.getValorInteres())
                .totalPagar(p.getTotalPagar())
                .cuotaDiaria(p.getCuotaDiaria())
                .saldoActual(p.getSaldoActual())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .estado(p.getEstado())
                .observacion(p.getObservacion())
                .cuotas(cuotasDto)
                .build();
    }
}
