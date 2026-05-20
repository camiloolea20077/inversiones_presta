package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.presupuesto.AbrirPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.MovimientoPresupuestoDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.MovimientoPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.PresupuestoResponseDto;
import com.cloud_technological.inversiones_prestar.entity.MovimientoPresupuestoEntity;
import com.cloud_technological.inversiones_prestar.entity.PresupuestoAdminEntity;
import com.cloud_technological.inversiones_prestar.repositories.presupuesto.MovimientoPresupuestoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.presupuesto.PresupuestoAdminJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.presupuesto.PresupuestoQueryRepository;
import com.cloud_technological.inversiones_prestar.services.AuditoriaService;
import com.cloud_technological.inversiones_prestar.services.PresupuestoService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresupuestoServiceImpl implements PresupuestoService {

    private final PresupuestoAdminJPARepository presupuestoRepository;
    private final MovimientoPresupuestoJPARepository movimientoRepository;
    private final PresupuestoQueryRepository presupuestoQueryRepository;
    private final AuditoriaService auditoriaService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public PresupuestoResponseDto abrir(AbrirPresupuestoRequestDto dto) {
        presupuestoRepository.findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .ifPresent(p -> {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "Ya existe un presupuesto activo. Use capitalizar o retirar para ajustarlo");
                });

        Long usuarioId = securityUtils.getUsuarioId();
        BigDecimal monto = escala(dto.getMontoInicial());

        PresupuestoAdminEntity presupuesto = presupuestoRepository.save(PresupuestoAdminEntity.builder()
                .montoInicial(monto)
                .fechaApertura(LocalDate.now())
                .observacion(limpiar(dto.getObservacion()))
                .estado("ACTIVO")
                .updatedBy(usuarioId)
                .build());

        registrarMovimiento(presupuesto.getId(), "APERTURA", monto,
                "Apertura del presupuesto inicial del administrador", usuarioId);

        auditoriaService.registrar("ABRIR_PRESUPUESTO", "presupuesto_admin",
                presupuesto.getId(), null, presupuesto,
                "Presupuesto inicial abierto por " + monto);

        return construirResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto capitalizar(MovimientoPresupuestoRequestDto dto) {
        PresupuestoAdminEntity presupuesto = obtenerActivoOrThrow();
        BigDecimal valor = escala(dto.getValor());
        Long usuarioId = securityUtils.getUsuarioId();

        registrarMovimiento(presupuesto.getId(), "CAPITALIZACION", valor,
                limpiar(dto.getObservacion()), usuarioId);

        auditoriaService.registrar("CAPITALIZAR_PRESUPUESTO", "presupuesto_admin",
                presupuesto.getId(), null, presupuesto,
                "Capitalización por " + valor);

        return construirResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto retirar(MovimientoPresupuestoRequestDto dto) {
        PresupuestoAdminEntity presupuesto = obtenerActivoOrThrow();
        BigDecimal valor = escala(dto.getValor());

        BigDecimal disponible = calcularSaldoDisponible(presupuesto.getId());
        if (valor.compareTo(disponible) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "No se puede retirar " + valor + ". Saldo disponible actual: " + disponible);
        }

        Long usuarioId = securityUtils.getUsuarioId();
        registrarMovimiento(presupuesto.getId(), "RETIRO", valor,
                limpiar(dto.getObservacion()), usuarioId);

        auditoriaService.registrar("RETIRAR_PRESUPUESTO", "presupuesto_admin",
                presupuesto.getId(), null, presupuesto,
                "Retiro por " + valor);

        return construirResponse(presupuesto);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoResponseDto obtenerActivo() {
        return presupuestoRepository.findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .map(this::construirResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal saldoDisponible() {
        return presupuestoRepository.findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .map(p -> calcularSaldoDisponible(p.getId()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarYDescontarPrestamo(BigDecimal montoSolicitado) {
        PresupuestoAdminEntity presupuesto = presupuestoRepository
                .findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "No hay un presupuesto del administrador configurado. "
                                + "Debes registrar un monto inicial antes de crear préstamos"));

        BigDecimal disponible = calcularSaldoDisponible(presupuesto.getId());
        BigDecimal solicitado = escala(montoSolicitado);

        if (solicitado.compareTo(disponible) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Saldo insuficiente en la caja del administrador. "
                            + "Disponible: " + disponible + ", solicitado: " + solicitado);
        }
        // No se descuenta físicamente: el saldo se calcula en tiempo real a partir
        // de préstamos y pagos, así que basta con haber validado el límite.
    }

    /* ===================== Helpers ===================== */

    private PresupuestoAdminEntity obtenerActivoOrThrow() {
        return presupuestoRepository.findFirstByEstadoAndDeletedAtIsNull("ACTIVO")
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "No existe un presupuesto activo. Debes abrirlo primero"));
    }

    private BigDecimal calcularSaldoDisponible(Long presupuestoId) {
        BigDecimal capital = presupuestoQueryRepository.capitalAportado(presupuestoId);
        BigDecimal prestado = presupuestoQueryRepository.totalPrestadoHistorico();
        BigDecimal recaudado = presupuestoQueryRepository.totalRecaudadoHistorico();
        return escala(capital.subtract(prestado).add(recaudado));
    }

    private void registrarMovimiento(Long presupuestoId, String tipo, BigDecimal valor,
            String observacion, Long usuarioId) {
        movimientoRepository.save(MovimientoPresupuestoEntity.builder()
                .presupuestoId(presupuestoId)
                .tipo(tipo)
                .valor(valor)
                .observacion(observacion)
                .fechaMovimiento(LocalDateTime.now())
                .updatedBy(usuarioId)
                .build());
    }

    private PresupuestoResponseDto construirResponse(PresupuestoAdminEntity p) {
        BigDecimal capital = presupuestoQueryRepository.capitalAportado(p.getId());
        BigDecimal prestado = presupuestoQueryRepository.totalPrestadoHistorico();
        BigDecimal recaudado = presupuestoQueryRepository.totalRecaudadoHistorico();
        BigDecimal capitalEnCalle = presupuestoQueryRepository.capitalEnCalle();
        BigDecimal saldoDisponible = escala(capital.subtract(prestado).add(recaudado));

        List<MovimientoPresupuestoDto> movimientos = movimientoRepository
                .findByPresupuestoIdAndDeletedAtIsNullOrderByFechaMovimientoDesc(p.getId())
                .stream()
                .map(m -> MovimientoPresupuestoDto.builder()
                        .id(m.getId())
                        .tipo(m.getTipo())
                        .valor(m.getValor())
                        .observacion(m.getObservacion())
                        .fechaMovimiento(m.getFechaMovimiento())
                        .build())
                .toList();

        return PresupuestoResponseDto.builder()
                .id(p.getId())
                .montoInicial(p.getMontoInicial())
                .fechaApertura(p.getFechaApertura())
                .observacion(p.getObservacion())
                .estado(p.getEstado())
                .capitalAportado(capital)
                .saldoDisponible(saldoDisponible)
                .totalPrestado(prestado)
                .totalRecaudado(recaudado)
                .capitalEnCalle(capitalEnCalle)
                .movimientos(movimientos)
                .build();
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }
}
