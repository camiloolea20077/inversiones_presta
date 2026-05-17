package com.cloud_technological.inversiones_prestar.repositories.mora;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.mora.MoraListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación del reporte de clientes en mora
 * (HU-BE-019 / HU-FE-021), usando NamedParameterJdbcTemplate.
 *
 * <p>Un préstamo está en mora cuando tiene al menos una cuota cuya fecha ya
 * pasó y aún conserva saldo (no está PAGADA ni ANULADA). El reporte agrupa
 * por préstamo activo y calcula los días de mora a partir de la cuota vencida
 * más antigua.</p>
 */
@Repository
public class MoraQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MoraQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<MoraListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("hoy", LocalDate.now());

        // Subconsulta: una fila por préstamo activo en mora, con los agregados de
        // las cuotas vencidas (fecha pasada, con saldo, no PAGADA/ANULADA).
        StringBuilder mora = new StringBuilder(
                " SELECT cp.prestamo_id, "
                + "        MIN(cp.fecha_cuota) AS cuota_vencida_mas_antigua, "
                + "        COUNT(*) AS cuotas_vencidas, "
                + "        SUM(cp.saldo_cuota) AS saldo_vencido "
                + " FROM cuotas_prestamo cp "
                + " WHERE cp.deleted_at IS NULL "
                + "   AND cp.fecha_cuota < :hoy "
                + "   AND cp.estado NOT IN ('PAGADA', 'ANULADA') "
                + "   AND cp.saldo_cuota > 0 "
                + " GROUP BY cp.prestamo_id ");

        StringBuilder where = new StringBuilder(" WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO' ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(c.nombre) LIKE :search ")
                    .append(" OR LOWER(COALESCE(c.documento, '')) LIKE :search ")
                    .append(" OR LOWER(COALESCE(t.nombre, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND p.ruta_id = :rutaId ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        String trabajadorId = obtenerParam(pageable, "trabajadorId");
        if (trabajadorId != null && !trabajadorId.isBlank()) {
            where.append(" AND p.trabajador_id = :trabajadorId ");
            params.addValue("trabajadorId", Long.valueOf(trabajadorId));
        }

        // Filtro por rango de días de mora (calculado desde la cuota más antigua).
        String diasMoraDesde = obtenerParam(pageable, "diasMoraDesde");
        if (diasMoraDesde != null && !diasMoraDesde.isBlank()) {
            where.append(" AND (:hoy - m.cuota_vencida_mas_antigua) >= :diasMoraDesde ");
            params.addValue("diasMoraDesde", Integer.valueOf(diasMoraDesde));
        }

        String diasMoraHasta = obtenerParam(pageable, "diasMoraHasta");
        if (diasMoraHasta != null && !diasMoraHasta.isBlank()) {
            where.append(" AND (:hoy - m.cuota_vencida_mas_antigua) <= :diasMoraHasta ");
            params.addValue("diasMoraHasta", Integer.valueOf(diasMoraHasta));
        }

        String baseFrom = " FROM prestamos p "
                + " JOIN ( " + mora + " ) m ON m.prestamo_id = p.id "
                + " JOIN clientes c ON c.id = p.cliente_id "
                + " LEFT JOIN rutas r ON r.id = p.ruta_id "
                + " LEFT JOIN trabajadores t ON t.id = p.trabajador_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT p.cliente_id, c.nombre AS cliente, c.documento, c.telefono, "
                + " p.ruta_id, r.nombre AS ruta, "
                + " p.trabajador_id, t.nombre AS trabajador, "
                + " p.id AS prestamo_id, p.saldo_actual AS saldo_pendiente, "
                + " m.cuota_vencida_mas_antigua, "
                + " (:hoy - m.cuota_vencida_mas_antigua) AS dias_mora, "
                + " m.cuotas_vencidas, m.saldo_vencido "
                + baseFrom + where
                + " ORDER BY dias_mora DESC, saldo_pendiente DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<MoraListDto> content = MapperRepository.mapListToDtoListNull(result, MoraListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows),
                total == null ? 0 : total);
    }

    @SuppressWarnings("unchecked")
    private String obtenerParam(PageableDto<Object> pageable, String key) {
        if (pageable.getParams() instanceof Map<?, ?> map) {
            Object value = ((Map<String, Object>) map).get(key);
            return value == null ? null : value.toString();
        }
        return null;
    }
}
