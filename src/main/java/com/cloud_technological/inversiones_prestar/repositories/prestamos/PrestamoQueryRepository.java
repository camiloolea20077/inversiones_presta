package com.cloud_technological.inversiones_prestar.repositories.prestamos;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de préstamos con NamedParameterJdbcTemplate.
 */
@Repository
public class PrestamoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PrestamoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<PrestamoListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE p.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(c.nombre) LIKE :search ")
                    .append(" OR LOWER(COALESCE(c.documento, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String estado = obtenerParam(pageable, "estado");
        if (estado != null && !estado.isBlank()) {
            where.append(" AND p.estado = :estado ");
            params.addValue("estado", estado.toUpperCase());
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND p.ruta_id = :rutaId ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        String baseFrom = " FROM prestamos p "
                + " JOIN clientes c ON c.id = p.cliente_id "
                + " LEFT JOIN rutas r ON r.id = p.ruta_id "
                + " LEFT JOIN trabajadores t ON t.id = p.trabajador_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT p.id, c.nombre AS cliente, p.cliente_id, "
                + " r.nombre AS ruta, t.nombre AS trabajador, "
                + " p.monto_prestado, p.total_pagar, p.saldo_actual, p.cuota_diaria, "
                + " p.plazo_dias, p.estado, p.fecha_inicio "
                + baseFrom + where
                + " ORDER BY p.created_at DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<PrestamoListDto> content = MapperRepository.mapListToDtoListNull(result, PrestamoListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows), total == null ? 0 : total);
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
