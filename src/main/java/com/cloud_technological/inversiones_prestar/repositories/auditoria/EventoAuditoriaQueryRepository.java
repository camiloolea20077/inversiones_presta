package com.cloud_technological.inversiones_prestar.repositories.auditoria;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaListDto;
import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaUsuarioDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación del registro de auditoría
 * (HU-BE-020 / HU-FE-022), usando NamedParameterJdbcTemplate.
 *
 * <p>Filtros soportados dentro de {@code params}: {@code usuarioId},
 * {@code accion}, {@code tablaAfectada}, {@code fecha} (un día concreto),
 * {@code fechaDesde} y {@code fechaHasta} (rango). Además {@code search}
 * busca por usuario, acción, tabla u observación.</p>
 */
@Repository
public class EventoAuditoriaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EventoAuditoriaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<AuditoriaListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE ea.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(COALESCE(ea.usuario_nombre, '')) LIKE :search ")
                    .append(" OR LOWER(ea.accion) LIKE :search ")
                    .append(" OR LOWER(ea.tabla_afectada) LIKE :search ")
                    .append(" OR LOWER(COALESCE(ea.observacion, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String usuarioId = obtenerParam(pageable, "usuarioId");
        if (usuarioId != null && !usuarioId.isBlank()) {
            where.append(" AND ea.usuario_id = :usuarioId ");
            params.addValue("usuarioId", Long.valueOf(usuarioId));
        }

        String accion = obtenerParam(pageable, "accion");
        if (accion != null && !accion.isBlank()) {
            where.append(" AND ea.accion = :accion ");
            params.addValue("accion", accion.trim().toUpperCase());
        }

        String tablaAfectada = obtenerParam(pageable, "tablaAfectada");
        if (tablaAfectada != null && !tablaAfectada.isBlank()) {
            where.append(" AND ea.tabla_afectada = :tablaAfectada ");
            params.addValue("tablaAfectada", tablaAfectada.trim());
        }

        String fecha = obtenerParam(pageable, "fecha");
        if (fecha != null && !fecha.isBlank()) {
            where.append(" AND ea.created_at::date = :fecha ");
            params.addValue("fecha", LocalDate.parse(fecha));
        }

        String fechaDesde = obtenerParam(pageable, "fechaDesde");
        if (fechaDesde != null && !fechaDesde.isBlank()) {
            where.append(" AND ea.created_at::date >= :fechaDesde ");
            params.addValue("fechaDesde", LocalDate.parse(fechaDesde));
        }

        String fechaHasta = obtenerParam(pageable, "fechaHasta");
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            where.append(" AND ea.created_at::date <= :fechaHasta ");
            params.addValue("fechaHasta", LocalDate.parse(fechaHasta));
        }

        String baseFrom = " FROM eventos_auditoria ea ";

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + baseFrom + where, params, Long.class);

        String sql = "SELECT ea.id, ea.created_at AS fecha, "
                + " ea.usuario_id, ea.usuario_nombre AS usuario, "
                + " ea.accion, ea.tabla_afectada, ea.registro_id, "
                + " ea.observacion, ea.valor_anterior, ea.valor_nuevo "
                + baseFrom + where
                + " ORDER BY ea.created_at DESC, ea.id DESC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<AuditoriaListDto> content = MapperRepository.mapListToDtoListNull(result, AuditoriaListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows),
                total == null ? 0 : total);
    }

    /** Usuarios distintos que tienen eventos de auditoría, para el filtro del reporte. */
    public List<AuditoriaUsuarioDto> usuariosConEventos() {
        String sql = "SELECT DISTINCT ea.usuario_id AS id, ea.usuario_nombre AS nombre "
                + " FROM eventos_auditoria ea "
                + " WHERE ea.deleted_at IS NULL AND ea.usuario_id IS NOT NULL "
                + " ORDER BY ea.usuario_nombre ";
        List<Map<String, Object>> result = jdbc.queryForList(sql, new MapSqlParameterSource());
        return MapperRepository.mapListToDtoListNull(result, AuditoriaUsuarioDto.class);
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
