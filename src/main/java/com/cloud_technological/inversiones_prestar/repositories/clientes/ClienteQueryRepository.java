package com.cloud_technological.inversiones_prestar.repositories.clientes;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteListDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.RutaClienteListDto;
import com.cloud_technological.inversiones_prestar.utils.MapperRepository;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

/**
 * Consultas de listado/paginación de clientes con NamedParameterJdbcTemplate.
 */
@Repository
public class ClienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ClienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<ClienteListDto> listar(PageableDto<Object> pageable) {
        long page = pageable.getPage() == null ? 0 : pageable.getPage();
        long rows = pageable.getRows() == null || pageable.getRows() <= 0 ? 10 : pageable.getRows();
        long offset = page * rows;

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" WHERE c.deleted_at IS NULL ");

        if (pageable.getSearch() != null && !pageable.getSearch().isBlank()) {
            where.append(" AND (LOWER(c.nombre) LIKE :search ")
                    .append(" OR LOWER(COALESCE(c.documento, '')) LIKE :search ")
                    .append(" OR LOWER(COALESCE(c.telefono, '')) LIKE :search) ");
            params.addValue("search", "%" + pageable.getSearch().toLowerCase() + "%");
        }

        String estado = obtenerParam(pageable, "estado");
        if (estado != null && !estado.isBlank()) {
            where.append(" AND c.estado = :estado ");
            params.addValue("estado", estado.toUpperCase());
        }

        String rutaId = obtenerParam(pageable, "rutaId");
        if (rutaId != null && !rutaId.isBlank()) {
            where.append(" AND EXISTS ( SELECT 1 FROM ruta_clientes rc "
                    + " WHERE rc.cliente_id = c.id AND rc.ruta_id = :rutaId "
                    + " AND rc.activo = TRUE AND rc.deleted_at IS NULL ) ");
            params.addValue("rutaId", Long.valueOf(rutaId));
        }

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clientes c " + where, params, Long.class);

        String sql = "SELECT c.id, c.documento, c.nombre, c.telefono, c.direccion, c.barrio, "
                + " c.estado, CAST(c.created_at AS DATE) AS fecha_registro, "
                + " ( SELECT r.nombre FROM ruta_clientes rc "
                + "   JOIN rutas r ON r.id = rc.ruta_id AND r.deleted_at IS NULL "
                + "   WHERE rc.cliente_id = c.id AND rc.activo = TRUE AND rc.deleted_at IS NULL "
                + "   ORDER BY rc.orden ASC LIMIT 1 ) AS ruta, "
                + " ( SELECT rc.ruta_id FROM ruta_clientes rc "
                + "   WHERE rc.cliente_id = c.id AND rc.activo = TRUE AND rc.deleted_at IS NULL "
                + "   ORDER BY rc.orden ASC LIMIT 1 ) AS ruta_id "
                + " FROM clientes c " + where
                + " ORDER BY c.nombre ASC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<ClienteListDto> content = MapperRepository.mapListToDtoListNull(result, ClienteListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows), total == null ? 0 : total);
    }

    /** Clientes de una ruta, en el orden del recorrido. */
    public List<RutaClienteListDto> listarClientesDeRuta(Long rutaId) {
        String sql = "SELECT rc.id, rc.cliente_id, rc.orden, "
                + " c.nombre, c.documento, c.telefono, c.direccion, c.barrio "
                + " FROM ruta_clientes rc "
                + " JOIN clientes c ON c.id = rc.cliente_id "
                + " WHERE rc.ruta_id = :rutaId AND rc.activo = TRUE AND rc.deleted_at IS NULL "
                + " ORDER BY rc.orden ASC ";
        List<Map<String, Object>> result = jdbc.queryForList(
                sql, new MapSqlParameterSource("rutaId", rutaId));
        return MapperRepository.mapListToDtoListNull(result, RutaClienteListDto.class);
    }

    /** Ejecuta la función de BD que inserta un cliente en una posición de la ruta. */
    public Long insertarClienteEnRuta(Long rutaId, Long clienteId, Integer ordenBase, Long usuarioId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("rutaId", rutaId)
                .addValue("clienteId", clienteId)
                .addValue("ordenBase", ordenBase)
                .addValue("usuarioId", usuarioId);
        return jdbc.queryForObject(
                "SELECT fn_insertar_cliente_en_ruta(:rutaId, :clienteId, :ordenBase, :usuarioId)",
                params, Long.class);
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
