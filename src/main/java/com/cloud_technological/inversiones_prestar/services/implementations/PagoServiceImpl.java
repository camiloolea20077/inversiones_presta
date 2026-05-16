package com.cloud_technological.inversiones_prestar.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.pagos.MarcarNoPagoDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.RegistrarPagoDto;
import com.cloud_technological.inversiones_prestar.entity.ClienteEntity;
import com.cloud_technological.inversiones_prestar.entity.CuotaPrestamoEntity;
import com.cloud_technological.inversiones_prestar.entity.PagoEntity;
import com.cloud_technological.inversiones_prestar.entity.PrestamoEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDetalleEntity;
import com.cloud_technological.inversiones_prestar.entity.RecaudoDiarioEntity;
import com.cloud_technological.inversiones_prestar.repositories.clientes.ClienteJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.pagos.PagoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.CuotaPrestamoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.prestamos.PrestamoJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDetalleJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.recaudos.RecaudoDiarioJPARepository;
import com.cloud_technological.inversiones_prestar.services.PagoService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final Set<String> FORMAS_PAGO =
            Set.of("EFECTIVO", "TRANSFERENCIA", "NEQUI", "DAVIPLATA", "OTRO");

    private final PagoJPARepository pagoRepository;
    private final RecaudoDetalleJPARepository detalleRepository;
    private final RecaudoDiarioJPARepository recaudoRepository;
    private final PrestamoJPARepository prestamoRepository;
    private final CuotaPrestamoJPARepository cuotaRepository;
    private final ClienteJPARepository clienteRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public PagoResponseDto registrarPago(RegistrarPagoDto dto) {
        Long usuarioId = securityUtils.getUsuarioId();

        RecaudoDetalleEntity detalle = detalleRepository.findByIdAndDeletedAtIsNull(dto.getRecaudoDetalleId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El renglón del recaudo no existe"));

        if (detalle.getPrestamoId() == null) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El cliente no tiene un préstamo activo para registrar el pago");
        }

        RecaudoDiarioEntity recaudo = recaudoRepository.findByIdAndDeletedAtIsNull(
                        detalle.getRecaudoDiarioId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Recaudo no encontrado"));
        if (!"ABIERTO".equals(recaudo.getEstado()) && !"EN_PROCESO".equals(recaudo.getEstado())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El recaudo del día ya fue cerrado");
        }

        PrestamoEntity prestamo = prestamoRepository.findByIdAndDeletedAtIsNull(detalle.getPrestamoId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));
        if ("ANULADO".equals(prestamo.getEstado()) || "PAGADO".equals(prestamo.getEstado())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "No se puede registrar un pago sobre un préstamo " + prestamo.getEstado());
        }

        BigDecimal valor = dto.getValorPago();
        if (valor.compareTo(prestamo.getSaldoActual()) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El valor del pago supera el saldo del préstamo (" + prestamo.getSaldoActual() + ")");
        }

        String formaPago = normalizarFormaPago(dto.getFormaPago());

        // Aplica el abono a las cuotas pendientes, de la más antigua a la más reciente.
        Long primeraCuotaId = aplicarACuotas(prestamo.getId(), valor, usuarioId);

        // Actualiza el saldo del préstamo.
        BigDecimal nuevoSaldo = prestamo.getSaldoActual().subtract(valor);
        prestamo.setSaldoActual(nuevoSaldo);
        if (nuevoSaldo.signum() == 0) {
            prestamo.setEstado("PAGADO");
        }
        prestamo.setUpdatedAt(LocalDateTime.now());
        prestamo.setUpdatedBy(usuarioId);
        prestamoRepository.save(prestamo);

        // Actualiza el renglón del recaudo.
        detalle.setValorPagado(detalle.getValorPagado().add(valor));
        detalle.setSaldoPendiente(nuevoSaldo);
        detalle.setEstado(detalle.getValorPagado().compareTo(detalle.getValorEsperado()) >= 0
                ? "PAGADO" : "PARCIAL");
        if (dto.getObservacion() != null && !dto.getObservacion().isBlank()) {
            detalle.setObservacion(dto.getObservacion());
        }
        detalle.setUpdatedAt(LocalDateTime.now());
        detalle.setUpdatedBy(usuarioId);
        detalleRepository.save(detalle);

        // Actualiza la planilla diaria.
        recaudo.setTotalRecaudado(recaudo.getTotalRecaudado().add(valor));
        recaudo.setEstado("EN_PROCESO");
        recaudo.setUpdatedAt(LocalDateTime.now());
        recaudo.setUpdatedBy(usuarioId);
        recaudoRepository.save(recaudo);

        // Registra el pago.
        PagoEntity pago = pagoRepository.save(PagoEntity.builder()
                .prestamoId(prestamo.getId())
                .cuotaPrestamoId(primeraCuotaId)
                .recaudoDetalleId(detalle.getId())
                .clienteId(detalle.getClienteId())
                .rutaId(recaudo.getRutaId())
                .trabajadorId(recaudo.getTrabajadorId())
                .fechaPago(LocalDateTime.now())
                .valorPago(valor)
                .formaPago(formaPago)
                .estado("APLICADO")
                .observacion(dto.getObservacion())
                .updatedBy(usuarioId)
                .build());

        // TODO: generar movimiento de caja PAGO_RECIBIDO al implementar el módulo de Caja.

        ClienteEntity cliente = clienteRepository.findById(detalle.getClienteId()).orElse(null);

        return PagoResponseDto.builder()
                .id(pago.getId())
                .prestamoId(prestamo.getId())
                .recaudoDetalleId(detalle.getId())
                .clienteId(detalle.getClienteId())
                .clienteNombre(cliente != null ? cliente.getNombre() : null)
                .valorPago(valor)
                .formaPago(formaPago)
                .estado(pago.getEstado())
                .fechaPago(pago.getFechaPago())
                .detalleEstado(detalle.getEstado())
                .saldoPrestamo(nuevoSaldo)
                .prestamoEstado(prestamo.getEstado())
                .build();
    }

    @Override
    @Transactional
    public PagoResponseDto marcarNoPago(MarcarNoPagoDto dto) {
        Long usuarioId = securityUtils.getUsuarioId();

        RecaudoDetalleEntity detalle = detalleRepository.findByIdAndDeletedAtIsNull(dto.getRecaudoDetalleId())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El renglón del recaudo no existe"));

        detalle.setEstado("NO_PAGO");
        if (dto.getObservacion() != null && !dto.getObservacion().isBlank()) {
            detalle.setObservacion(dto.getObservacion());
        }
        detalle.setUpdatedAt(LocalDateTime.now());
        detalle.setUpdatedBy(usuarioId);
        detalleRepository.save(detalle);

        ClienteEntity cliente = clienteRepository.findById(detalle.getClienteId()).orElse(null);

        return PagoResponseDto.builder()
                .recaudoDetalleId(detalle.getId())
                .clienteId(detalle.getClienteId())
                .clienteNombre(cliente != null ? cliente.getNombre() : null)
                .detalleEstado(detalle.getEstado())
                .saldoPrestamo(detalle.getSaldoPendiente())
                .build();
    }

    /* ===================== Helpers ===================== */

    /** Distribuye el abono entre las cuotas pendientes y devuelve la primera afectada. */
    private Long aplicarACuotas(Long prestamoId, BigDecimal valor, Long usuarioId) {
        List<CuotaPrestamoEntity> cuotas = cuotaRepository
                .findByPrestamoIdAndDeletedAtIsNullOrderByNumeroCuotaAsc(prestamoId);

        BigDecimal restante = valor;
        Long primeraCuotaId = null;

        for (CuotaPrestamoEntity cuota : cuotas) {
            if (restante.signum() <= 0) {
                break;
            }
            if ("PAGADA".equals(cuota.getEstado()) || "ANULADA".equals(cuota.getEstado())) {
                continue;
            }
            BigDecimal abono = restante.min(cuota.getSaldoCuota());
            cuota.setValorPagado(cuota.getValorPagado().add(abono));
            cuota.setSaldoCuota(cuota.getSaldoCuota().subtract(abono));
            cuota.setEstado(cuota.getSaldoCuota().signum() == 0 ? "PAGADA" : "PARCIAL");
            cuota.setUpdatedAt(LocalDateTime.now());
            cuota.setUpdatedBy(usuarioId);
            if (primeraCuotaId == null) {
                primeraCuotaId = cuota.getId();
            }
            restante = restante.subtract(abono);
        }

        cuotaRepository.saveAll(cuotas);
        return primeraCuotaId;
    }

    private String normalizarFormaPago(String forma) {
        if (forma == null || forma.isBlank()) {
            return "EFECTIVO";
        }
        String f = forma.trim().toUpperCase();
        if (!FORMAS_PAGO.contains(f)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Forma de pago no válida. Use EFECTIVO, TRANSFERENCIA, NEQUI, DAVIPLATA u OTRO");
        }
        return f;
    }
}
