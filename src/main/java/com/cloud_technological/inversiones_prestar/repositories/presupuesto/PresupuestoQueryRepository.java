package com.cloud_technological.inversiones_prestar.repositories.presupuesto;

import java.math.BigDecimal;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Consultas agregadas para el cálculo del saldo disponible y la
 * rentabilidad del presupuesto del administrador.
 */
@Repository
public class PresupuestoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PresupuestoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Capital aportado neto = aperturas + capitalizaciones - retiros.
     * Considera todos los movimientos del presupuesto recibido.
     */
    public BigDecimal capitalAportado(Long presupuestoId) {
        MapSqlParameterSource params = new MapSqlParameterSource("presupuestoId", presupuestoId);
        String sql = "SELECT COALESCE(SUM(CASE WHEN tipo IN ('APERTURA', 'CAPITALIZACION', 'AJUSTE')"
                + " THEN valor ELSE -valor END), 0) "
                + " FROM movimientos_presupuesto "
                + " WHERE presupuesto_id = :presupuestoId AND deleted_at IS NULL ";
        return scalar(sql, params);
    }

    /** Total prestado histórico (capital desembolsado en préstamos no anulados). */
    public BigDecimal totalPrestadoHistorico() {
        String sql = "SELECT COALESCE(SUM(monto_prestado), 0) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado <> 'ANULADO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Total recaudado histórico (todos los pagos aplicados). */
    public BigDecimal totalRecaudadoHistorico() {
        String sql = "SELECT COALESCE(SUM(valor_pago), 0) FROM pagos "
                + " WHERE deleted_at IS NULL AND estado = 'APLICADO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /**
     * Saldo total por cobrar de los préstamos vigentes (cartera activa). Es
     * lo que el negocio espera recibir aún (capital + intereses).
     */
    public BigDecimal carteraActiva() {
        String sql = "SELECT COALESCE(SUM(saldo_actual), 0) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'ACTIVO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Suma de intereses presupuestados de todos los préstamos no anulados. */
    public BigDecimal interesesProyectadosTotales() {
        String sql = "SELECT COALESCE(SUM(valor_interes), 0) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado <> 'ANULADO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /**
     * Intereses ya cobrados (proporcionales). Para cada préstamo no anulado:
     *     valor_interes * (total_pagar - saldo_actual) / total_pagar.
     */
    public BigDecimal interesesCobrados() {
        String sql = "SELECT COALESCE(SUM(valor_interes * (total_pagar - saldo_actual) "
                + "   / NULLIF(total_pagar, 0)), 0) "
                + " FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado <> 'ANULADO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Capital aún en la calle: monto_prestado * saldo_actual / total_pagar. */
    public BigDecimal capitalEnCalle() {
        String sql = "SELECT COALESCE(SUM(monto_prestado * saldo_actual "
                + "   / NULLIF(total_pagar, 0)), 0) "
                + " FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'ACTIVO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Cantidad de préstamos activos. */
    public Long prestamosActivos() {
        String sql = "SELECT COUNT(*) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'ACTIVO' ";
        Long value = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return value == null ? 0L : value;
    }

    /** Cantidad de préstamos pagados. */
    public Long prestamosPagados() {
        String sql = "SELECT COUNT(*) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'PAGADO' ";
        Long value = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return value == null ? 0L : value;
    }

    /**
     * Total en mora: suma de saldo_cuota de cuotas vencidas no pagadas
     * a la fecha indicada.
     */
    public BigDecimal totalEnMora(java.time.LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT COALESCE(SUM(cp.saldo_cuota), 0) "
                + " FROM cuotas_prestamo cp "
                + " JOIN prestamos p ON p.id = cp.prestamo_id AND p.deleted_at IS NULL "
                + " WHERE cp.deleted_at IS NULL "
                + " AND p.estado = 'ACTIVO' "
                + " AND cp.fecha_cuota < :fecha "
                + " AND cp.estado NOT IN ('PAGADA', 'ANULADA') "
                + " AND cp.saldo_cuota > 0 ";
        return scalar(sql, params);
    }

    /** Cantidad de clientes con cuotas vencidas. */
    public Long clientesEnMora(java.time.LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT COUNT(DISTINCT p.cliente_id) "
                + " FROM cuotas_prestamo cp "
                + " JOIN prestamos p ON p.id = cp.prestamo_id AND p.deleted_at IS NULL "
                + " WHERE cp.deleted_at IS NULL "
                + " AND p.estado = 'ACTIVO' "
                + " AND cp.fecha_cuota < :fecha "
                + " AND cp.estado NOT IN ('PAGADA', 'ANULADA') "
                + " AND cp.saldo_cuota > 0 ";
        Long value = jdbc.queryForObject(sql, params, Long.class);
        return value == null ? 0L : value;
    }

    private BigDecimal scalar(String sql, MapSqlParameterSource params) {
        BigDecimal value = jdbc.queryForObject(sql, params, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }
}
