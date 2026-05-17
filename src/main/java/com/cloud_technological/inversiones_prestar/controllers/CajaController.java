package com.cloud_technological.inversiones_prestar.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.caja.AbrirCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaDiariaResponseDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CajaListDto;
import com.cloud_technological.inversiones_prestar.dto.caja.CerrarCajaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaDto;
import com.cloud_technological.inversiones_prestar.dto.caja.MovimientoCajaListDto;
import com.cloud_technological.inversiones_prestar.services.CajaService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<CajaDiariaResponseDto>> abrir(
            @Valid @RequestBody AbrirCajaRequestDto dto) {
        CajaDiariaResponseDto result = cajaService.abrir(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Caja diaria abierta", false, result));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<ApiResponse<CajaDiariaResponseDto>> cerrar(
            @Valid @RequestBody CerrarCajaRequestDto dto) {
        CajaDiariaResponseDto result = cajaService.cerrar(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Caja diaria cerrada", false, result));
    }

    /**
     * Listado paginado de cajas diarias de los trabajadores con filtros
     * opcionales (trabajadorId, rutaId, estado, fechaCaja, fechaDesde,
     * fechaHasta) — HU-FE-020.
     */
    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<CajaListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<CajaListDto> result = cajaService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Listado de cajas diarias", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CajaDiariaResponseDto>> obtener(@PathVariable Long id) {
        CajaDiariaResponseDto result = cajaService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Caja encontrada", false, result));
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public ResponseEntity<ApiResponse<CajaDiariaResponseDto>> porTrabajador(
            @PathVariable Long trabajadorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        CajaDiariaResponseDto result = cajaService.obtenerPorTrabajador(trabajadorId, fecha);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Caja del trabajador", false, result));
    }

    @GetMapping("/mi-caja")
    public ResponseEntity<ApiResponse<CajaDiariaResponseDto>> miCaja(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        CajaDiariaResponseDto result = cajaService.miCaja(fecha);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Caja diaria del trabajador", false, result));
    }

    /** Movimientos de una caja en orden cronológico (HU-BE-016). */
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<ApiResponse<List<MovimientoCajaDto>>> movimientos(
            @PathVariable Long id) {
        List<MovimientoCajaDto> result = cajaService.movimientosPorCaja(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Movimientos de la caja", false, result));
    }

    /**
     * Listado paginado de movimientos de caja con filtros opcionales
     * (cajaDiariaId, trabajadorId, rutaId, tipoMovimiento, fechaCaja,
     * fechaDesde, fechaHasta) — HU-BE-016.
     */
    @PostMapping("/movimientos/listar")
    public ResponseEntity<ApiResponse<Page<MovimientoCajaListDto>>> listarMovimientos(
            @RequestBody PageableDto<Object> pageable) {
        Page<MovimientoCajaListDto> result = cajaService.listarMovimientos(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Listado de movimientos de caja", false, result));
    }
}
