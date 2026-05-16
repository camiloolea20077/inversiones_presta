package com.cloud_technological.inversiones_prestar.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.rutas.AsignarTrabajadorDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaComboDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaListDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaRequestDto;
import com.cloud_technological.inversiones_prestar.dto.rutas.RutaResponseDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.CambiarEstadoDto;
import com.cloud_technological.inversiones_prestar.services.RutaService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
public class RutaController {

    private final RutaService rutaService;

    @PostMapping
    public ResponseEntity<ApiResponse<RutaResponseDto>> crear(
            @Valid @RequestBody RutaRequestDto dto) {
        RutaResponseDto result = rutaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Ruta creada", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDto>> actualizar(
            @PathVariable Long id, @Valid @RequestBody RutaRequestDto dto) {
        RutaResponseDto result = rutaService.actualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ruta actualizada", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaResponseDto>> obtener(@PathVariable Long id) {
        RutaResponseDto result = rutaService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ruta encontrada", false, result));
    }

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<RutaListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<RutaListDto> result = rutaService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Listado de rutas", false, result));
    }

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<RutaComboDto>>> listarActivas() {
        List<RutaComboDto> result = rutaService.listarActivas();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Rutas activas", false, result));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<RutaResponseDto>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoDto dto) {
        RutaResponseDto result = rutaService.cambiarEstado(id, dto.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Estado actualizado", false, result));
    }

    @PostMapping("/{id}/asignar-trabajador")
    public ResponseEntity<ApiResponse<RutaResponseDto>> asignarTrabajador(
            @PathVariable Long id, @Valid @RequestBody AsignarTrabajadorDto dto) {
        RutaResponseDto result = rutaService.asignarTrabajador(id, dto.getTrabajadorId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Trabajador asignado", false, result));
    }
}
