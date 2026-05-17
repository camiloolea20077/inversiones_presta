package com.cloud_technological.inversiones_prestar.repositories.caja;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.caja.CajaListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de cajas diarias de los trabajadores
 * con NamedParameterJdbcTemplate (HU-FE-020).
 */
@Repository
public class CajaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CajaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<CajaListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE cd.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(COALESCE(t.nombre, '')) LIKE :search ")
                    .append(" OR LOWER(COALESCE(r.nombre, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String trabajadorId = obtenerParam(pageable, "trabajadorId");
        if (trabajadorId != null && !trabajadorId.isBlank()) {
            where.append(" AND cd.trabajador_id = :trabajadorId ");
            params.addValue("trabajadorId", Long.valueOf(trabajadorId));
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND cd.ruta_id = :rutaId ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        String estado = obtenerParam(pageable, "estado");
        if (estado != null && !estado.isBlank()) {
            where.append(" AND cd.estado = :estado ");
            params.addValue("estado", estado.toUpperCase());
        }

        String fechaCaja = obtenerParam(pageable, "fechaCaja");
        if (fechaCaja != null && !fechaCaja.isBlank()) {
            where.append(" AND cd.fecha_caja = :fechaCaja ");
            params.addValue("fechaCaja", fechaCaja);
        }

        String fechaDesde = obtenerParam(pageable, "fechaDesde");
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            where.append(" AND cd.fecha_caja >= :fechaDesde ");
            params.addValue("fechaDesde", fechaDesde);
        }

        String fechaHasta = obtenerParam(pageable, "fechaHasta");
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            where.append(" AND cd.fecha_caja <= :fechaHasta ");
            params.addValue("fechaHasta", fechaHasta);
        }

        String baseFrom = " FROM cajas_diarias cd "
                + " LEFT JOIN trabajadores t ON t.id = cd.trabajador_id "
                + " LEFT JOIN rutas r ON r.id = cd.ruta_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT cd.id, cd.trabajador_id, t.nombre AS trabajador, "
                + " cd.ruta_id, r.nombre AS ruta, cd.fecha_caja, "
                + " cd.valor_inicial, cd.valor_prestamos_entregados, cd.valor_recaudado, "
                + " cd.valor_esperado_cierre, cd.valor_entregado, cd.diferencia, "
                + " cd.estado, cd.observacion "
                + baseFrom + where
                + " ORDER BY cd.fecha_caja DESC, cd.id DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<CajaListDto> content =
                MapperRepository.mapListToDtoListNull(result, CajaListDto.class);

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
