package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.caja.AbrirCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaDiariaResponseDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaListDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CerrarCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaListDto;
import com.cloud_technological.inversiones_prestar.entity.CajaDiariaEntity;
import com.cloud_technological.inversiones_prestar.entity.MovimientoCajaEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaEntity;
import com.cloud_technological.inversiones_prestar.entity.RutaTrabajadorEntity;
import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;
import com.cloud_technological.inversiones_prestar.repositories.caja.CajaDiariaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.caja.CajaQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.caja.MovimientoCajaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.caja.MovimientoCajaQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.rutas.RutaTrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.services.AuditoriaService;
import com.cloud_technological.inversiones_prestar.services.CajaService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaDiariaJPARepository cajaRepository;
    private final CajaQueryRepository cajaQueryRepository;
    private final MovimientoCajaJPARepository movimientoRepository;
    private final MovimientoCajaQueryRepository movimientoQueryRepository;
    private final TrabajadorJPARepository trabajadorRepository;
    private final RutaJPARepository rutaRepository;
    private final RutaTrabajadorJPARepository rutaTrabajadorRepository;
    private final AuditoriaService auditoriaService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public CajaDiariaResponseDto abrir(AbrirCajaRequestDto dto) {
        TrabajadorEntity trabajador = trabajadorRepository
                .findByIdAndDeletedAtIsNull(dto.getTrabajadorId())
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El trabajador no existe"));

        LocalDate dia = dto.getFecha() != null ? dto.getFecha() : LocalDate.now();

        Long rutaId = dto.getRutaId();
        if (rutaId == null) {
            rutaId = rutaTrabajadorRepository
                    .findFirstByTrabajadorIdAndActivoTrueAndDeletedAtIsNullOrderByFechaInicioDesc(
                            trabajador.getId())
                    .map(RutaTrabajadorEntity::getRutaId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                            "El trabajador no tiene una ruta asignada"));
        }
        RutaEntity ruta = rutaRepository.findByIdAndDeletedAtIsNull(rutaId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "La ruta no existe"));

        // No debe permitir abrir dos cajas vigentes el mismo día.
        cajaRepository.findFirstByTrabajadorIdAndFechaCajaAndEstadoNotAndDeletedAtIsNull(
                        trabajador.getId(), dia, "ANULADA")
                .ifPresent(c -> {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "El trabajador ya tiene una caja para esa fecha");
                });

        Long usuarioId = securityUtils.getUsuarioId();
        BigDecimal valorInicial = escala(dto.getValorInicial());

        CajaDiariaEntity caja = cajaRepository.save(CajaDiariaEntity.builder()
                .trabajadorId(trabajador.getId())
                .rutaId(ruta.getId())
                .fechaCaja(dia)
                .valorInicial(valorInicial)
                .valorPrestamosEntregados(BigDecimal.ZERO)
                .valorRecaudado(BigDecimal.ZERO)
                .valorEsperadoCierre(valorInicial)
                .valorEntregado(BigDecimal.ZERO)
                .diferencia(BigDecimal.ZERO)
                .estado("ABIERTA")
                .observacion(limpiar(dto.getObservacion()))
                .updatedBy(usuarioId)
                .build());

        // Movimiento CAJA_INICIAL (HU-BE-016).
        if (valorInicial.signum() > 0) {
            registrarMovimiento(caja, "CAJA_INICIAL", valorInicial,
                    "CAJA_DIARIA", caja.getId(), "Apertura de caja diaria", usuarioId);
        }

        return construirResponse(caja);
    }

    @Override
    @Transactional
    public CajaDiariaResponseDto cerrar(CerrarCajaRequestDto dto) {
        CajaDiariaEntity caja = cajaRepository.findByIdAndDeletedAtIsNull(dto.getCajaId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Caja no encontrada"));

        // No debe permitir cerrar dos veces la misma caja.
        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La caja ya fue cerrada y no puede cerrarse de nuevo");
        }

        Long usuarioId = securityUtils.getUsuarioId();
        BigDecimal valorEntregado = escala(dto.getValorEntregado());
        BigDecimal esperado = caja.getValorEsperadoCierre();
        BigDecimal diferencia = escala(valorEntregado.subtract(esperado));

        caja.setValorEntregado(valorEntregado);
        caja.setDiferencia(diferencia);
        caja.setEstado(diferencia.signum() == 0 ? "CERRADA" : "CERRADA_CON_DIFERENCIA");
        if (dto.getObservacion() != null && !dto.getObservacion().isBlank()) {
            caja.setObservacion(dto.getObservacion().trim());
        }
        caja.setUpdatedAt(LocalDateTime.now());
        caja.setUpdatedBy(usuarioId);
        cajaRepository.save(caja);

        // Movimiento CIERRE_CAJA (HU-BE-016).
        if (valorEntregado.signum() > 0) {
            registrarMovimiento(caja, "CIERRE_CAJA", valorEntregado,
                    "CAJA_DIARIA", caja.getId(), "Cierre de caja diaria", usuarioId);
        }

        // Auditoría (HU-BE-020): cierre de caja.
        auditoriaService.registrar("CERRAR_CAJA", "cajas_diarias", caja.getId(), null, caja,
                "Cierre de caja: esperado " + esperado + ", entregado " + valorEntregado
                        + ", diferencia " + diferencia);

        return construirResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiariaResponseDto obtener(Long id) {
        CajaDiariaEntity caja = cajaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Caja no encontrada"));
        return construirResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaListDto> listar(PageableDto<Object> pageable) {
        return cajaQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiariaResponseDto obtenerPorTrabajador(Long trabajadorId, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        CajaDiariaEntity caja = cajaRepository
                .findFirstByTrabajadorIdAndFechaCajaAndEstadoNotAndDeletedAtIsNull(
                        trabajadorId, dia, "ANULADA")
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El trabajador no tiene caja abierta para esa fecha"));
        return construirResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiariaResponseDto miCaja(LocalDate fecha) {
        Long usuarioId = securityUtils.getUsuarioId();
        if (usuarioId == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado");
        }
        TrabajadorEntity trabajador = trabajadorRepository
                .findByUsuarioIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El usuario no está asociado a un trabajador"));
        return obtenerPorTrabajador(trabajador.getId(), fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoCajaDto> movimientosPorCaja(Long cajaId) {
        // Valida que la caja exista; no se eliminan movimientos de caja.
        cajaRepository.findByIdAndDeletedAtIsNull(cajaId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Caja no encontrada"));
        return movimientoRepository
                .findByCajaDiariaIdAndDeletedAtIsNullOrderByFechaMovimientoAsc(cajaId)
                .stream()
                .map(this::aMovimientoDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoCajaListDto> listarMovimientos(PageableDto<Object> pageable) {
        return movimientoQueryRepository.listar(pageable);
    }

    /* ===================== Integración con préstamos y pagos ===================== */

    @Override
    @Transactional
    public void registrarPrestamoEntregado(Long trabajadorId, Long rutaId, LocalDate fecha,
            BigDecimal valor, Long prestamoId, Long usuarioId) {
        CajaDiariaEntity caja = cajaAbierta(trabajadorId, fecha);
        if (caja == null || valor == null || valor.signum() <= 0) {
            return;
        }
        BigDecimal monto = escala(valor);
        caja.setValorPrestamosEntregados(caja.getValorPrestamosEntregados().add(monto));
        recalcularEsperado(caja);
        caja.setUpdatedAt(LocalDateTime.now());
        caja.setUpdatedBy(usuarioId);
        cajaRepository.save(caja);

        registrarMovimiento(caja, "PRESTAMO_ENTREGADO", monto,
                "PRESTAMO", prestamoId, "Préstamo entregado al cliente", usuarioId);
    }

    @Override
    @Transactional
    public void registrarPagoRecibido(Long trabajadorId, Long rutaId, LocalDate fecha,
            BigDecimal valor, Long pagoId, Long usuarioId) {
        CajaDiariaEntity caja = cajaAbierta(trabajadorId, fecha);
        if (caja == null || valor == null || valor.signum() <= 0) {
            return;
        }
        BigDecimal monto = escala(valor);
        caja.setValorRecaudado(caja.getValorRecaudado().add(monto));
        recalcularEsperado(caja);
        caja.setUpdatedAt(LocalDateTime.now());
        caja.setUpdatedBy(usuarioId);
        cajaRepository.save(caja);

        registrarMovimiento(caja, "PAGO_RECIBIDO", monto,
                "PAGO", pagoId, "Pago recibido del cliente", usuarioId);
    }

    /* ===================== Helpers ===================== */

    /** Caja ABIERTA del trabajador para la fecha, o null si no hay. */
    private CajaDiariaEntity cajaAbierta(Long trabajadorId, LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return cajaRepository
                .findFirstByTrabajadorIdAndFechaCajaAndEstadoNotAndDeletedAtIsNull(
                        trabajadorId, dia, "ANULADA")
                .filter(c -> "ABIERTA".equals(c.getEstado()))
                .orElse(null);
    }

    /** valor_esperado_cierre = valor_inicial - valor_prestamos_entregados + valor_recaudado. */
    private void recalcularEsperado(CajaDiariaEntity caja) {
        BigDecimal esperado = caja.getValorInicial()
                .subtract(caja.getValorPrestamosEntregados())
                .add(caja.getValorRecaudado());
        caja.setValorEsperadoCierre(escala(esperado));
    }

    private void registrarMovimiento(CajaDiariaEntity caja, String tipo, BigDecimal valor,
            String referenciaTipo, Long referenciaId, String observacion, Long usuarioId) {
        movimientoRepository.save(MovimientoCajaEntity.builder()
                .cajaDiariaId(caja.getId())
                .trabajadorId(caja.getTrabajadorId())
                .rutaId(caja.getRutaId())
                .tipoMovimiento(tipo)
                .valor(escala(valor))
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .fechaMovimiento(LocalDateTime.now())
                .observacion(observacion)
                .updatedBy(usuarioId)
                .build());
    }

    private CajaDiariaResponseDto construirResponse(CajaDiariaEntity caja) {
        String trabajadorNombre = trabajadorRepository.findById(caja.getTrabajadorId())
                .map(TrabajadorEntity::getNombre)
                .orElse(null);
        String rutaNombre = rutaRepository.findById(caja.getRutaId())
                .map(RutaEntity::getNombre)
                .orElse(null);

        List<MovimientoCajaDto> movimientos = movimientoRepository
                .findByCajaDiariaIdAndDeletedAtIsNullOrderByFechaMovimientoAsc(caja.getId())
                .stream()
                .map(this::aMovimientoDto)
                .toList();

        return CajaDiariaResponseDto.builder()
                .id(caja.getId())
                .trabajadorId(caja.getTrabajadorId())
                .trabajadorNombre(trabajadorNombre)
                .rutaId(caja.getRutaId())
                .rutaNombre(rutaNombre)
                .fechaCaja(caja.getFechaCaja())
                .valorInicial(caja.getValorInicial())
                .valorPrestamosEntregados(caja.getValorPrestamosEntregados())
                .valorRecaudado(caja.getValorRecaudado())
                .valorEsperadoCierre(caja.getValorEsperadoCierre())
                .valorEntregado(caja.getValorEntregado())
                .diferencia(caja.getDiferencia())
                .estado(caja.getEstado())
                .observacion(caja.getObservacion())
                .movimientos(movimientos)
                .build();
    }

    private MovimientoCajaDto aMovimientoDto(MovimientoCajaEntity m) {
        return MovimientoCajaDto.builder()
                .id(m.getId())
                .tipoMovimiento(m.getTipoMovimiento())
                .valor(m.getValor())
                .referenciaTipo(m.getReferenciaTipo())
                .referenciaId(m.getReferenciaId())
                .fechaMovimiento(m.getFechaMovimiento())
                .observacion(m.getObservacion())
                .build();
    }

    private String limpiar(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
