package com.cloud_technological.inversiones_prestar.repositories.pagos;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.pagos.PagoListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de pagos con NamedParameterJdbcTemplate
 * (HU-FE-019 — consulta de pagos del administrador).
 */
@Repository
public class PagoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PagoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<PagoListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE p.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(c.nombre) LIKE :search ")
                    .append(" OR LOWER(COALESCE(c.documento, '')) LIKE :search ")
                    .append(" OR LOWER(COALESCE(t.nombre, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String trabajadorId = obtenerParam(pageable, "trabajadorId");
        if (trabajadorId != null && !trabajadorId.isBlank()) {
            where.append(" AND p.trabajador_id = :trabajadorId ");
            params.addValue("trabajadorId", Long.valueOf(trabajadorId));
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND p.ruta_id = :rutaId ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        String estado = obtenerParam(pageable, "estado");
        if (estado != null && !estado.isBlank()) {
            where.append(" AND p.estado = :estado ");
            params.addValue("estado", estado.toUpperCase());
        }

        // Filtro por una fecha puntual.
        String fecha = obtenerParam(pageable, "fecha");
        if (fecha != null && !fecha.isBlank()) {
            where.append(" AND p.fecha_pago::date = :fecha ");
            params.addValue("fecha", fecha);
        }

        // Filtro por rango de fechas (opcional, complementa a fecha puntual).
        String fechaDesde = obtenerParam(pageable, "fechaDesde");
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            where.append(" AND p.fecha_pago >= :fechaDesde ");
            params.addValue("fechaDesde", fechaDesde + " 00:00:00");
        }

        String fechaHasta = obtenerParam(pageable, "fechaHasta");
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            where.append(" AND p.fecha_pago <= :fechaHasta ");
            params.addValue("fechaHasta", fechaHasta + " 23:59:59");
        }

        String baseFrom = " FROM pagos p "
                + " JOIN clientes c ON c.id = p.cliente_id "
                + " LEFT JOIN trabajadores t ON t.id = p.trabajador_id "
                + " LEFT JOIN rutas r ON r.id = p.ruta_id ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT p.id, p.fecha_pago, p.cliente_id, c.nombre AS cliente, "
                + " p.trabajador_id, t.nombre AS trabajador, "
                + " p.ruta_id, r.nombre AS ruta, "
                + " p.valor_pago, p.forma_pago, p.estado, p.prestamo_id "
                + baseFrom + where
                + " ORDER BY p.fecha_pago DESC, p.id DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<PagoListDto> content = MapperRepository.mapListToDtoListNull(result, PagoListDto.class);

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
