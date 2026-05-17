# Estado de las Historias de Usuario

Control de avance del Sistema de Recaudo Diario. Estados: ✅ listo · 🟡 parcial · ⬜ pendiente.

> Lo actualiza el agente `implementar-hu` cada vez que se completa una HU.

## Backend (HU-BE)

| HU | Descripción | Estado |
|----|-------------|--------|
| HU-BE-001 | Gestión de roles y usuarios | ✅ |
| HU-BE-002 | Autenticación con JWT | ✅ |
| HU-BE-003 | Gestión de trabajadores | ✅ |
| HU-BE-004 | Gestión de rutas | ✅ |
| HU-BE-005 | Asignación de trabajador a ruta | ✅ |
| HU-BE-006 | Gestión de clientes | ✅ |
| HU-BE-007 | Insertar cliente en ruta con orden | ✅ |
| HU-BE-008 | Configuración de límites por trabajador | ✅ |
| HU-BE-009 | Crear préstamo desde la ruta | ✅ |
| HU-BE-010 | Generación automática de cuotas | ✅ |
| HU-BE-011 | Generar recaudo diario por ruta | ✅ |
| HU-BE-012 | Registrar pago de cliente | ✅ |
| HU-BE-013 | Marcar cliente como no pagó | ✅ |
| HU-BE-014 | Crear cliente y préstamo en una sola operación | ✅ |
| HU-BE-015 | Control de caja diaria del trabajador | ✅ |
| HU-BE-016 | Movimientos de caja | ✅ (4 tipos generados + endpoints de consulta `GET /api/caja/{id}/movimientos` y `POST /api/caja/movimientos/listar`; anulaciones quedan fuera de alcance hasta que exista el módulo de anulaciones) |
| HU-BE-017 | Consulta de ruta diaria del trabajador | ✅ |
| HU-BE-018 | Dashboard administrativo | ✅ (`GET /api/dashboard/resumen?fecha=` — total prestado, recaudado del día, cartera activa, clientes en mora, rutas/trabajadores activos, recaudo esperado vs real + resumen por ruta) |
| HU-BE-019 | Reporte de clientes en mora | ✅ (`POST /api/mora/listar` — préstamos activos con cuotas vencidas; muestra ruta, trabajador, días de mora y saldo; filtros `rutaId`, `trabajadorId`, `diasMoraDesde`, `diasMoraHasta` y `search`) |
| HU-BE-020 | Auditoría general | ✅ (tabla `eventos_auditoria` + `AuditoriaService` defensivo en transacción `REQUIRES_NEW`; eventos `CREAR` cliente/préstamo, `PAGAR`, `REORDENAR`/`INSERTAR_EN_RUTA`, `CERRAR_CAJA` con valor anterior/nuevo en JSON; endpoints `POST /api/auditoria/listar` y `GET /api/auditoria/usuarios`) |

## Frontend (HU-FE)

| HU | Descripción | Estado |
|----|-------------|--------|
| HU-FE-001 | Login del sistema | ✅ |
| HU-FE-002 | Layout para administrador | ✅ |
| HU-FE-003 | Layout móvil para trabajador | ✅ |
| HU-FE-004 | Vista de ruta diaria del trabajador | ✅ |
| HU-FE-005 | Tarjeta de cliente en ruta | ✅ |
| HU-FE-006 | Registrar pago | ✅ |
| HU-FE-007 | Registrar pago parcial | ✅ |
| HU-FE-008 | Marcar cliente como no pagó | ✅ |
| HU-FE-009 | Agregar cliente cerca | ✅ |
| HU-FE-010 | Crear cliente y préstamo desde ruta | ✅ (cubierto por el asistente de 2 pasos de HU-FE-009, ampliado con simulación completa) |
| HU-FE-011 | Resumen de caja del trabajador | ✅ |
| HU-FE-012 | Dashboard administrativo | ✅ (conectado a `GET /api/dashboard/resumen`; KPIs, recaudo esperado vs real, resumen por ruta y navegación a detalles) |
| HU-FE-013 | Gestión de trabajadores | ✅ |
| HU-FE-014 | Configuración de límites del trabajador | ✅ |
| HU-FE-015 | Gestión de rutas | ✅ |
| HU-FE-016 | Ordenamiento de clientes en ruta | ✅ |
| HU-FE-017 | Gestión de clientes | ✅ |
| HU-FE-018 | Gestión de préstamos | ✅ |
| HU-FE-019 | Consulta de pagos | ✅ (vista admin `/admin/pagos`; backend `POST /api/pagos/listar` + `GET /api/pagos/{id}`) |
| HU-FE-020 | Cierre de caja administrativo | ✅ (vista admin `/admin/caja`; lista las cajas diarias de los trabajadores con filtros por fecha, trabajador, ruta y estado; diálogo de cierre con caja inicial, préstamos, recaudo, valor esperado, captura del valor entregado, diferencia en vivo y observación obligatoria si hay diferencia; backend `POST /api/caja/listar` + `GET /api/caja/{id}` + `POST /api/caja/cerrar`) |
| HU-FE-021 | Reporte de clientes en mora | ✅ (vista admin `/admin/mora`; tabla cliente/ruta/trabajador/días de mora/saldo pendiente con filtros por ruta, trabajador y rango de días de mora) |
| HU-FE-022 | Auditoría del sistema | ✅ (vista admin `/admin/auditoria`; lista fecha, usuario, acción, tabla, registro y observación; filtros por usuario, acción y fecha; diálogo de detalle con valor anterior vs valor nuevo en JSON) |

## Pendientes técnicos

- Movimiento de caja `PAGO_RECIBIDO`: RESUELTO — `PagoServiceImpl#registrarPago` llama a `CajaService.registrarPagoRecibido` (suma `valor_recaudado` + movimiento).
- Movimiento de caja `PRESTAMO_ENTREGADO`: RESUELTO — `PrestamoServiceImpl#crear` y `#crearClienteConPrestamo` llaman a `CajaService.registrarPrestamoEntregado` (suma `valor_prestamos_entregados` + movimiento).
- HU-FE-010: cubierta dentro del asistente de 2 pasos de HU-FE-009 en la ruta diaria del trabajador. No se creó una vista admin redundante; se amplió la simulación (valor prestado, interés, total, cuota) y se hace auto-cálculo al cambiar los datos.
- Los movimientos de caja se suman a la caja del trabajador SOLO si hay una caja `ABIERTA` para esa fecha; si el trabajador no abrió caja, el préstamo/pago se registra igual (la operación no se bloquea).
- HU-BE-016: RESUELTO. Los 4 tipos de movimiento (`CAJA_INICIAL`, `PRESTAMO_ENTREGADO`, `PAGO_RECIBIDO`, `CIERRE_CAJA`) se persisten con tipo, valor, referencia y fecha, y no se eliminan (no hay borrado de movimientos). Se añadieron `GET /api/caja/{id}/movimientos` (movimientos de una caja en orden cronológico) y `POST /api/caja/movimientos/listar` (listado paginado con filtros `cajaDiariaId`, `trabajadorId`, `rutaId`, `tipoMovimiento`, `fechaCaja`, `fechaDesde`, `fechaHasta`). PENDIENTE real: movimientos `ANULACION_PAGO` / `ANULACION_PRESTAMO`, que dependen del módulo de anulaciones aún no construido.
- HU-FE-011: RESUELTO. Vista móvil `features/trabajador/caja-trabajador` en la ruta `/trabajador/caja`. Muestra caja inicial, préstamos entregados, recaudo recibido y valor esperado de cierre; lista los movimientos del día; permite abrir caja si no hay caja del día (`POST /api/caja/abrir`) y cerrar caja si está abierta (`POST /api/caja/cerrar`). Acceso desde la barra inferior de la ruta diaria. La ruta `trabajador` pasó a tener hijos: `ruta` (planilla diaria) y `caja`.
- HU-FE-020 (cierre de caja admin): RESUELTO. Backend: se añadió el listado paginado de cajas diarias siguiendo las convenciones de los demás listados — `CajaQueryRepository` (NamedParameterJdbcTemplate; filtros `trabajadorId`, `rutaId`, `estado`, `fechaCaja`, `fechaDesde`, `fechaHasta` y `search` por trabajador/ruta), `CajaListDto` (snake_case) y el endpoint `POST /api/caja/listar`. Se reutilizan `GET /api/caja/{id}` y `POST /api/caja/cerrar` ya existentes; el backend ya validaba que una caja no `ABIERTA` no puede cerrarse de nuevo. Frontend: vista admin `/admin/caja` (reemplaza el placeholder del menú "Caja diaria"); tabla con trabajador, ruta, fecha, caja inicial, préstamos entregados, recaudado, valor esperado, valor entregado, diferencia (sobrante azul / faltante rojo) y estado (badge Abierta/Cerrada/Con diferencia); filtros por fecha, trabajador, ruta y estado. Al seleccionar una caja se abre un diálogo de cierre que muestra el desglose del dinero (caja inicial, préstamos, recaudo, valor esperado), captura el valor entregado, calcula la diferencia en vivo y exige observación cuando la diferencia es distinta de cero; el botón "Cerrar caja" llama a `POST /api/caja/cerrar`. Si la caja ya está `CERRADA`/`CERRADA_CON_DIFERENCIA` el diálogo es de solo lectura (no se ofrece el botón de cerrar). El `caja.service.ts`/`caja.model.ts` reutilizados de HU-FE-011 se ampliaron con `listar`, `obtener` y la interfaz `CajaRow`.
- DDL de caja: el backend no genera DDL. Las tablas `cajas_diarias` y `movimientos_caja` ya están en el esquema base; se añadió un script idempotente en `docs/sql/hu-be-015_caja_diaria.sql`.
- HU-FE-019 (consulta de pagos): RESUELTO. No existía HU-BE específica; el módulo de pagos solo tenía registro (HU-BE-012). Se añadió al backend el listado/detalle de pagos siguiendo las convenciones de otros listados: `PagoQueryRepository` (NamedParameterJdbcTemplate, filtros `fecha`, `fechaDesde`, `fechaHasta`, `trabajadorId`, `rutaId`, `estado` y `search` por cliente/documento/trabajador), `PagoListDto` (snake_case), `PagoDetalleDto`, y los endpoints `POST /api/pagos/listar` y `GET /api/pagos/{id}`. La vista admin `/admin/pagos` lista fecha/cliente/trabajador/ruta/forma de pago/valor/estado, permite filtrar por fecha, trabajador y ruta, y abre un diálogo de detalle del pago (cliente, trabajador, ruta, préstamo asociado, cuota aplicada y observación). El criterio "identificar pagos anulados/reversados" se cumple mostrando el `estado` del pago: los pagos con estado `ANULADO`/`REVERSADO` se resaltan en la tabla (fila tachada en rojo) y en el detalle. PENDIENTE real: la funcionalidad de anular/reversar un pago no existe todavía (no hay módulo de anulaciones); todos los pagos registrados quedan en estado `APLICADO`.
- HU-BE-019 / HU-FE-021 (reporte de clientes en mora): RESUELTO. Se reúsa la misma definición de mora del dashboard (HU-BE-018): préstamo `ACTIVO` con al menos una cuota cuya fecha ya pasó (`fecha_cuota < hoy`), no `PAGADA`/`ANULADA` y con `saldo_cuota > 0`. Backend: `MoraQueryRepository` (NamedParameterJdbcTemplate; subconsulta agregada por préstamo que calcula cuota vencida más antigua, cantidad de cuotas vencidas y saldo vencido; los días de mora = `hoy - cuota_vencida_mas_antigua`), `MoraListDto` (snake_case), `MoraService(+Impl)`, `MoraController` con `POST /api/mora/listar`. Filtros soportados en `params`: `rutaId`, `trabajadorId`, `diasMoraDesde`, `diasMoraHasta`, más `search` por cliente/documento/trabajador. El reporte agrupa por préstamo activo en mora (un cliente con varios préstamos en mora aparece una fila por préstamo). Frontend: vista admin `/admin/mora` (reemplaza el placeholder de la entrada de menú "Mora"); tabla con #, cliente (avatar + documento), ruta, trabajador, fecha de la cuota más vencida, cuotas vencidas, días de mora (badge ámbar/naranja/rojo según gravedad: <8 baja, 8-29 media, >=30 alta), saldo vencido y saldo pendiente; pie con el total de saldo vencido de la página; filtros por ruta, trabajador y rango de días de mora (validación: el "desde" no puede superar al "hasta"). PENDIENTE técnico: no hay exportación del reporte (el botón "Exportar" del topbar es global y aún no implementado); el reporte es de solo lectura.
- HU-BE-020 / HU-FE-022 (auditoría): RESUELTO. Backend: tabla `eventos_auditoria` (script idempotente en `docs/sql/hu-be-020_auditoria.sql`; el backend no genera DDL, ejecutar a mano) con `usuario_id`, `usuario_nombre` (cacheado), `accion`, `tabla_afectada`, `registro_id`, `valor_anterior`/`valor_nuevo` (JSON en texto) y `observacion`. `AuditoriaService#registrar` es defensivo: serializa los valores con Jackson, persiste en transacción `REQUIRES_NEW` y captura cualquier fallo sin romper la operación de negocio. Se cablearon los puntos de la HU: creación de cliente (`ClienteServiceImpl#crear` y `PrestamoServiceImpl#crearClienteConPrestamo`), creación de préstamo (`PrestamoServiceImpl#crear` y `#crearClienteConPrestamo`), registro de pago (`PagoServiceImpl#registrarPago`), cambios de orden en ruta (`RutaClienteServiceImpl#insertarEnRuta` y `#reordenar`) y cierre de caja (`CajaServiceImpl#cerrar`). Endpoints: `POST /api/auditoria/listar` (filtros `usuarioId`, `accion`, `tablaAfectada`, `fecha`, `fechaDesde`, `fechaHasta` y `search`) y `GET /api/auditoria/usuarios` (usuarios con eventos, para el filtro). Frontend: vista admin `/admin/auditoria` (reemplaza el placeholder del menú "Auditoría"); tabla con fecha/hora, usuario (avatar), acción (badge por tipo), tabla y registro afectado, observación; filtros por usuario, acción y fecha; diálogo de detalle que muestra el valor anterior vs el valor nuevo formateados como JSON. PENDIENTE real: el evento `ANULAR` está contemplado en los filtros pero no se dispara todavía porque el módulo de anulaciones no existe; tampoco se audita la actualización de registros (solo creación/acciones sensibles según la HU).
- HU-BE-018 / HU-FE-012 (dashboard): RESUELTO. Endpoint `GET /api/dashboard/resumen?fecha=` (opcional, por defecto hoy) que devuelve `DashboardResumenDto`. El indicador "clientes en mora" se calcula como `COUNT(DISTINCT cliente_id)` de préstamos activos con cuotas vencidas (fecha pasada, no PAGADA/ANULADA y con saldo > 0); el reporte detallado de mora sigue siendo HU-BE-019. La vista admin del dashboard quedó conectada al endpoint real (se eliminó la data quemada). Se reemplazó el gráfico de mora por días (dependía de HU-BE-019) por un comparativo de barras esperado vs real por ruta. La tabla "Pagos recientes" se retiró del dashboard hasta tener el módulo de pagos admin (HU-FE-019); el dashboard cumple los criterios de HU-FE-012 con KPIs, recaudo esperado vs real, resumen por ruta y navegación a detalles.
