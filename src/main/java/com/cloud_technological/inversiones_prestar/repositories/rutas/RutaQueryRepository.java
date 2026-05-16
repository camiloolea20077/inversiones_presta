package com.cloud_technological.inversiones_prestar.repositories.rutas;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.rutas.RutaListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de rutas con NamedParameterJdbcTemplate.
 */
@Repository
public class RutaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RutaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<RutaListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE r.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(r.nombre) LIKE :search ")
                    .append(" OR LOWER(COALESCE(r.zona, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String estado = obtenerParam(pageable, "estado");
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            where.append(" AND r.activo = TRUE ");
        } else if ("INACTIVO".equalsIgnoreCase(estado)) {
            where.append(" AND r.activo = FALSE ");
        }

        String zona = obtenerParam(pageable, "zona");
        if (zona != null && !zona.isBlank()) {
            where.append(" AND LOWER(COALESCE(r.zona, '')) = :zona ");
            params.addValue("zona", zona.toLowerCase());
        }

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM rutas r " + where, params, Long.class);

        String sql = "SELECT r.id, r.nombre, r.zona, r.descripcion, "
                + " CAST(r.created_at AS DATE) AS fecha_creacion, "
                + " CASE WHEN r.activo THEN 'ACTIVO' ELSE 'INACTIVO' END AS estado, "
                + " ( SELECT t.nombre FROM ruta_trabajadores rt "
                + "   JOIN trabajadores t ON t.id = rt.trabajador_id "
                + "   WHERE rt.ruta_id = r.id AND rt.activo = TRUE AND rt.deleted_at IS NULL "
                + "   ORDER BY rt.fecha_inicio DESC LIMIT 1 ) AS trabajador, "
                + " ( SELECT rt.trabajador_id FROM ruta_trabajadores rt "
                + "   WHERE rt.ruta_id = r.id AND rt.activo = TRUE AND rt.deleted_at IS NULL "
                + "   ORDER BY rt.fecha_inicio DESC LIMIT 1 ) AS trabajador_id, "
                + " ( SELECT COUNT(*) FROM ruta_clientes rc "
                + "   WHERE rc.ruta_id = r.id AND rc.activo = TRUE AND rc.deleted_at IS NULL ) AS clientes "
                + " FROM rutas r " + where
                + " ORDER BY r.nombre ASC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<RutaListDto> content = MapperRepository.mapListToDtoListNull(result, RutaListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows), total == null ? 0 : total);
    }

    /** Cantidad de clientes activos asignados a una ruta. */
    public int contarClientes(Long rutaId) {
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ruta_clientes WHERE ruta_id = :id "
                        + " AND activo = TRUE AND deleted_at IS NULL",
                new MapSqlParameterSource("id", rutaId), Long.class);
        return total == null ? 0 : total.intValue();
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
