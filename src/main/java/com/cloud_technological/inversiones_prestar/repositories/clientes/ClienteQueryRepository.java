package com.cloud_technological.inversiones_prestar.repositories.clientes;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteDetalleDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteListDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClientePagoDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClientePrestamoDto;
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

        // Filtro por préstamo: SI (con préstamo activo) / NO (sin préstamo activo).
        String conPrestamo = obtenerParam(pageable, "conPrestamo");
        if (conPrestamo != null && !conPrestamo.isBlank()) {
            String existe = "EXISTS ( SELECT 1 FROM prestamos p "
                    + " WHERE p.cliente_id = c.id AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL )";
            where.append("SI".equalsIgnoreCase(conPrestamo)
                    ? " AND " + existe + " "
                    : " AND NOT " + existe + " ");
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
                + "   ORDER BY rc.orden ASC LIMIT 1 ) AS ruta_id, "
                + " ( SELECT p.saldo_actual FROM prestamos p "
                + "   WHERE p.cliente_id = c.id AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL "
                + "   ORDER BY p.fecha_inicio DESC, p.id DESC LIMIT 1 ) AS prestamo_activo, "
                + " ( SELECT COUNT(*) FROM prestamos p "
                + "   WHERE p.cliente_id = c.id AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL "
                + " ) AS prestamos_activos, "
                + " ( SELECT COALESCE(MAX(CURRENT_DATE - cp.fecha_cuota), 0) "
                + "   FROM cuotas_prestamo cp "
                + "   JOIN prestamos p ON p.id = cp.prestamo_id "
                + "   WHERE p.cliente_id = c.id AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL "
                + "     AND cp.deleted_at IS NULL AND cp.fecha_cuota < CURRENT_DATE "
                + "     AND cp.estado NOT IN ('PAGADA', 'ANULADA') AND cp.saldo_cuota > 0 "
                + " ) AS dias_mora "
                + " FROM clientes c " + where
                + " ORDER BY c.nombre ASC "
                + " LIMIT :limit OFFSET :offset ";

        params.addValue("limit", rows);
        params.addValue("offset", offset);

        List<Map<String, Object>> result = jdbc.queryForList(sql, params);
        List<ClienteListDto> content = MapperRepository.mapListToDtoListNull(result, ClienteListDto.class);

        return new PageImpl<>(content, PageRequest.of((int) page, (int) rows), total == null ? 0 : total);
    }

    /**
     * Detalle completo del cliente para la pantalla "Clientes y Préstamos":
     * datos del cliente, resumen de cartera, préstamo activo e historial de
     * pagos. Devuelve {@code null} si el cliente no existe.
     */
    public ClienteDetalleDto detalle(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        String baseSql = "SELECT c.id, c.documento, c.nombre, c.telefono, c.direccion, "
                + " c.barrio, c.observacion, c.estado, c.activo, "
                + " CAST(c.created_at AS DATE) AS fecha_registro, "
                + " ( SELECT r.nombre FROM ruta_clientes rc "
                + "   JOIN rutas r ON r.id = rc.ruta_id AND r.deleted_at IS NULL "
                + "   WHERE rc.cliente_id = c.id AND rc.activo = TRUE AND rc.deleted_at IS NULL "
                + "   ORDER BY rc.orden ASC LIMIT 1 ) AS ruta "
                + " FROM clientes c WHERE c.id = :id AND c.deleted_at IS NULL ";
        List<Map<String, Object>> base = jdbc.queryForList(baseSql, params);
        if (base.isEmpty()) {
            return null;
        }
        ClienteDetalleDto dto = MapperRepository
                .mapListToDtoListNull(base, ClienteDetalleDto.class).get(0);

        // Resumen de cartera del cliente.
        Map<String, Object> resumen = jdbc.queryForMap(
                "SELECT "
                        + " ( SELECT COUNT(*) FROM prestamos p "
                        + "   WHERE p.cliente_id = :id AND p.deleted_at IS NULL ) AS prestamos_totales, "
                        + " ( SELECT COUNT(*) FROM prestamos p "
                        + "   WHERE p.cliente_id = :id AND p.estado = 'ACTIVO' "
                        + "     AND p.deleted_at IS NULL ) AS prestamos_activos, "
                        + " ( SELECT COALESCE(SUM(pg.valor_pago), 0) FROM pagos pg "
                        + "   WHERE pg.cliente_id = :id AND pg.deleted_at IS NULL "
                        + "     AND pg.estado = 'APLICADO' ) AS total_pagado ",
                params);
        dto.setPrestamos_totales(((Number) resumen.get("prestamos_totales")).intValue());
        dto.setPrestamos_activos(((Number) resumen.get("prestamos_activos")).intValue());
        dto.setTotal_pagado(new java.math.BigDecimal(resumen.get("total_pagado").toString()));

        // Préstamo activo más reciente, con su progreso.
        String prestamoSql = "SELECT p.id, p.monto_prestado, p.tasa_porcentaje, p.tipo_interes, "
                + " p.valor_interes, p.total_pagar, p.cuota_diaria, p.saldo_actual, "
                + " p.plazo_dias, p.fecha_inicio, p.fecha_fin, p.estado, "
                + " ( SELECT COUNT(*) FROM cuotas_prestamo cp "
                + "   WHERE cp.prestamo_id = p.id AND cp.deleted_at IS NULL ) AS cuotas_total, "
                + " ( SELECT COUNT(*) FROM cuotas_prestamo cp "
                + "   WHERE cp.prestamo_id = p.id AND cp.deleted_at IS NULL "
                + "     AND cp.estado = 'PAGADA' ) AS cuotas_pagadas, "
                + " GREATEST(CURRENT_DATE - p.fecha_inicio, 0) AS dias_transcurridos, "
                + " ( SELECT COALESCE(MAX(CURRENT_DATE - cp.fecha_cuota), 0) "
                + "   FROM cuotas_prestamo cp "
                + "   WHERE cp.prestamo_id = p.id AND cp.deleted_at IS NULL "
                + "     AND cp.fecha_cuota < CURRENT_DATE "
                + "     AND cp.estado NOT IN ('PAGADA', 'ANULADA') "
                + "     AND cp.saldo_cuota > 0 ) AS dias_mora "
                + " FROM prestamos p "
                + " WHERE p.cliente_id = :id AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL "
                + " ORDER BY p.fecha_inicio DESC, p.id DESC LIMIT 1 ";
        List<Map<String, Object>> prestamo = jdbc.queryForList(prestamoSql, params);
        if (!prestamo.isEmpty()) {
            ClientePrestamoDto pa = MapperRepository
                    .mapListToDtoListNull(prestamo, ClientePrestamoDto.class).get(0);
            dto.setPrestamo_activo(pa);
            dto.setDias_mora(pa.getDias_mora());
            dto.setCuotas_pagadas(pa.getCuotas_pagadas());
            dto.setCuotas_totales(pa.getCuotas_total());
        } else {
            dto.setDias_mora(0);
            dto.setCuotas_pagadas(0);
            dto.setCuotas_totales(0);
        }

        // Últimos pagos del cliente.
        List<Map<String, Object>> pagos = jdbc.queryForList(
                "SELECT pg.id, pg.fecha_pago, pg.valor_pago, pg.forma_pago, pg.estado "
                        + " FROM pagos pg "
                        + " WHERE pg.cliente_id = :id AND pg.deleted_at IS NULL "
                        + " ORDER BY pg.fecha_pago DESC, pg.id DESC LIMIT 5 ",
                params);
        dto.setPagos_recientes(MapperRepository.mapListToDtoListNull(pagos, ClientePagoDto.class));

        return dto;
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
