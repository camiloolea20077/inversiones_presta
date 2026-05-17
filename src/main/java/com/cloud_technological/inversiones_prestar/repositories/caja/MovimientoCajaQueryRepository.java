package com.cloud_technological.inversiones_prestar.repositories.caja;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de movimientos de caja con
 * NamedParameterJdbcTemplate (HU-BE-016).
 */
@Repository
public class MovimientoCajaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MovimientoCajaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<MovimientoCajaListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE m.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(m.tipo_movimiento) LIKE :search ")
                    .append(" OR LOWER(COALESCE(m.observacion, '')) LIKE :search ")
                    .append(" OR LOWER(COALESCE(t.nombre, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String cajaDiariaId = obtenerParam(pageable, "cajaDiariaId");
        if (cajaDiariaId != null && !cajaDiariaId.isBlank()) {
            where.append(" AND m.caja_diaria_id = :cajaDiariaId ");
            params.addValue("cajaDiariaId", Long.valueOf(cajaDiariaId));
        }

        String trabajadorId = obtenerParam(pageable, "trabajadorId");
        if (trabajadorId != null && !trabajadorId.isBlank()) {
            where.append(" AND m.trabajador_id = :trabajadorId ");
            params.addValue("trabajadorId", Long.valueOf(trabajadorId));
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND m.ruta_id = :rutaId ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        String tipoMovimiento = obtenerParam(pageable, "tipoMovimiento");
        if (tipoMovimiento != null && !tipoMovimiento.isBlank()) {
            where.append(" AND m.tipo_movimiento = :tipoMovimiento ");
            params.addValue("tipoMovimiento", tipoMovimiento.toUpperCase());
        }

        String fechaDesde = obtenerParam(pageable, "fechaDesde");
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            where.append(" AND m.fecha_movimiento >= :fechaDesde ");
            params.addValue("fechaDesde", fechaDesde + " 00:00:00");
        }

        String fechaHasta = obtenerParam(pageable, "fechaHasta");
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            where.append(" AND m.fecha_movimiento <= :fechaHasta ");
            params.addValue("fechaHasta", fechaHasta + " 23:59:59");
        }

        String fechaCaja = obtenerParam(pageable, "fechaCaja");
        if (fechaCaja != null && !fechaCaja.isBlank()) {
            where.append(" AND cd.fecha_caja = :fechaCaja ");
            params.addValue("fechaCaja", fechaCaja);
        }

        String baseFrom = " FROM movimientos_caja m "
                + " JOIN cajas_diarias cd ON cd.id = m.caja_diaria_id "
                + " LEFT JOIN trabajadores t ON t.id = m.trabajador_id "
                + " LEFT JOIN rutas r ON r.id = m.ruta_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT m.id, m.caja_diaria_id, m.trabajador_id, t.nombre AS trabajador, "
                + " m.ruta_id, r.nombre AS ruta, cd.fecha_caja, "
                + " m.tipo_movimiento, m.valor, m.referencia_tipo, m.referencia_id, "
                + " m.fecha_movimiento, m.observacion "
                + baseFrom + where
                + " ORDER BY m.fecha_movimiento DESC, m.id DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<MovimientoCajaListDto> content =
                MapperRepository.mapListToDtoListNull(result, MovimientoCajaListDto.class);

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
