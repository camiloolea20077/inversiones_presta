package com.cloud_technological.inversiones_prestar.repositories.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.dashboard.DashboardRutaDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;

/**
 * Consultas agregadas de indicadores del negocio para el dashboard
 * administrativo (HU-BE-018), usando NamedParameterJdbcTemplate.
 */
@Repository
public class DashboardQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Suma del monto prestado de los préstamos activos. */
    public BigDecimal totalPrestado() {
        String sql = "SELECT COALESCE(SUM(monto_prestado), 0) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'ACTIVO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Cartera activa = suma de saldos pendientes de préstamos activos. */
    public BigDecimal carteraActiva() {
        String sql = "SELECT COALESCE(SUM(saldo_actual), 0) FROM prestamos "
                + " WHERE deleted_at IS NULL AND estado = 'ACTIVO' ";
        return scalar(sql, new MapSqlParameterSource());
    }

    /** Total recaudado en la fecha indicada (pagos aplicados). */
    public BigDecimal totalRecaudado(LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT COALESCE(SUM(valor_pago), 0) FROM pagos "
                + " WHERE deleted_at IS NULL AND estado = 'APLICADO' "
                + " AND CAST(fecha_pago AS DATE) = :fecha ";
        return scalar(sql, params);
    }

    /**
     * Clientes en mora: clientes con préstamo activo y al menos una cuota
     * cuya fecha ya pasó y aún tiene saldo (no está pagada ni anulada).
     */
    public Long clientesEnMora(LocalDate fecha) {
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

    /** Cantidad de rutas activas. */
    public Long rutasActivas() {
        String sql = "SELECT COUNT(*) FROM rutas WHERE deleted_at IS NULL AND activo = TRUE ";
        Long value = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return value == null ? 0L : value;
    }

    /** Cantidad de trabajadores activos. */
    public Long trabajadoresActivos() {
        String sql = "SELECT COUNT(*) FROM trabajadores WHERE deleted_at IS NULL AND activo = TRUE ";
        Long value = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return value == null ? 0L : value;
    }

    /** Recaudo esperado del día (suma de total_esperado de las planillas no anuladas). */
    public BigDecimal recaudoEsperado(LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT COALESCE(SUM(total_esperado), 0) FROM recaudos_diarios "
                + " WHERE deleted_at IS NULL AND estado <> 'ANULADO' "
                + " AND fecha_recaudo = :fecha ";
        return scalar(sql, params);
    }

    /** Recaudo real del día (suma de total_recaudado de las planillas no anuladas). */
    public BigDecimal recaudoReal(LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT COALESCE(SUM(total_recaudado), 0) FROM recaudos_diarios "
                + " WHERE deleted_at IS NULL AND estado <> 'ANULADO' "
                + " AND fecha_recaudo = :fecha ";
        return scalar(sql, params);
    }

    /**
     * Resumen del recaudo del día por cada ruta activa: trabajador asignado,
     * clientes activos, esperado, recaudado y progreso.
     */
    public List<DashboardRutaDto> resumenRutas(LocalDate fecha) {
        MapSqlParameterSource params = new MapSqlParameterSource("fecha", fecha);
        String sql = "SELECT r.id AS ruta_id, r.nombre AS ruta, r.zona, "
                + " t.nombre AS trabajador, "
                + " (SELECT COUNT(*) FROM ruta_clientes rc "
                + "    WHERE rc.ruta_id = r.id AND rc.deleted_at IS NULL AND rc.activo = TRUE) AS clientes, "
                + " COALESCE(rd.total_esperado, 0) AS total_esperado, "
                + " COALESCE(rd.total_recaudado, 0) AS total_recaudado, "
                + " CASE WHEN COALESCE(rd.total_esperado, 0) > 0 "
                + "      THEN LEAST(100, ROUND(rd.total_recaudado * 100 / rd.total_esperado)) "
                + "      ELSE 0 END AS progreso, "
                + " COALESCE(rd.estado, 'SIN_PLANILLA') AS estado "
                + " FROM rutas r "
                + " LEFT JOIN ruta_trabajadores rt ON rt.ruta_id = r.id "
                + "      AND rt.deleted_at IS NULL AND rt.activo = TRUE "
                + " LEFT JOIN trabajadores t ON t.id = rt.trabajador_id AND t.deleted_at IS NULL "
                + " LEFT JOIN recaudos_diarios rd ON rd.ruta_id = r.id "
                + "      AND rd.deleted_at IS NULL AND rd.estado <> 'ANULADO' "
                + "      AND rd.fecha_recaudo = :fecha "
                + " WHERE r.deleted_at IS NULL AND r.activo = TRUE "
                + " ORDER BY r.nombre ";

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        return MapperRepository.mapListToDtoListNull(result, DashboardRutaDto.class);
    }

    private BigDecimal scalar(String sql, MapSqlParameterSource params) {
        BigDecimal value = jdbc.queryForObject(sql, params, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }
}
