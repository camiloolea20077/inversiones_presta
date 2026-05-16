package com.cloud_technological.inversiones_prestar.repositories.trabajadores;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de trabajadores.
 * Usa NamedParameterJdbcTemplate (no JPA) para devolver proyecciones livianas.
 */
@Repository
public class TrabajadorQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TrabajadorQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<TrabajadorListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder where = new StringBuilder(" WHERE t.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(t.nombre) LIKE :search ")
                    .append(" OR LOWER(t.documento) LIKE :search ")
                    .append(" OR LOWER(COALESCE(t.telefono, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String estado = obtenerParam(pageable, "estado");
        if ("ACTIVO".equalsIgnoreCase(estado)) {
            where.append(" AND t.activo = TRUE ");
        } else if ("INACTIVO".equalsIgnoreCase(estado)) {
            where.append(" AND t.activo = FALSE ");
        }

        String baseFrom = " FROM trabajadores t "
                + " LEFT JOIN usuarios u ON u.id = t.usuario_id AND u.deleted_at IS NULL "
                + " LEFT JOIN roles r ON r.id = u.rol_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT t.id, t.documento, t.nombre, t.telefono, t.direccion, "
                + " CAST(t.created_at AS DATE) AS fecha_ingreso, "
                + " CASE WHEN t.activo THEN 'ACTIVO' ELSE 'INACTIVO' END AS estado, "
                + " u.nombre AS usuario_nombre, u.email AS usuario_correo, r.nombre AS rol, "
                + " CASE WHEN EXISTS ("
                + "   SELECT 1 FROM limites_trabajador lt "
                + "   WHERE lt.trabajador_id = t.id AND lt.activo = TRUE AND lt.deleted_at IS NULL"
                + " ) THEN 'SI' ELSE 'NO' END AS tiene_limite, "
                + " ( SELECT r2.nombre FROM ruta_trabajadores rt "
                + "   JOIN rutas r2 ON r2.id = rt.ruta_id AND r2.deleted_at IS NULL "
                + "   WHERE rt.trabajador_id = t.id AND rt.activo = TRUE AND rt.deleted_at IS NULL "
                + "   ORDER BY rt.fecha_inicio DESC LIMIT 1 ) AS ruta_asignada, "
                + " ( SELECT COUNT(*) FROM ruta_clientes rc "
                + "   WHERE rc.activo = TRUE AND rc.deleted_at IS NULL "
                + "   AND rc.ruta_id IN ( SELECT rt.ruta_id FROM ruta_trabajadores rt "
                + "     WHERE rt.trabajador_id = t.id AND rt.activo = TRUE AND rt.deleted_at IS NULL ) "
                + " ) AS clientes, "
                + " ( SELECT COALESCE(SUM(p.valor_pago), 0) FROM pagos p "
                + "   WHERE p.trabajador_id = t.id AND p.estado = 'APLICADO' "
                + "   AND p.deleted_at IS NULL AND CAST(p.fecha_pago AS DATE) = CURRENT_DATE "
                + " ) AS recaudo_hoy "
                + baseFrom + where
                + " ORDER BY t.nombre ASC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<TrabajadorListDto> content = MapperRepository.mapListToDtoListNull(result, TrabajadorListDto.class);

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
