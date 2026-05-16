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

import com.cloud_technological.inversiones_prestar.dto.trabajadores.CambiarEstadoDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.LimiteTrabajadorResponseDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorComboDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorListDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorResponseDto;
import com.cloud_technological.inversiones_prestar.services.LimiteTrabajadorService;
import com.cloud_technological.inversiones_prestar.services.TrabajadorService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trabajadores")
@RequiredArgsConstructor
public class TrabajadorController {

    private final TrabajadorService trabajadorService;
    private final LimiteTrabajadorService limiteTrabajadorService;

    @PostMapping
    public ResponseEntity<ApiResponse<TrabajadorResponseDto>> crear(
            @Valid @RequestBody TrabajadorRequestDto dto) {
        TrabajadorResponseDto result = trabajadorService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Trabajador creado", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrabajadorResponseDto>> actualizar(
            @PathVariable Long id, @Valid @RequestBody TrabajadorRequestDto dto) {
        TrabajadorResponseDto result = trabajadorService.actualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Trabajador actualizado", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrabajadorResponseDto>> obtener(@PathVariable Long id) {
        TrabajadorResponseDto result = trabajadorService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Trabajador encontrado", false, result));
    }

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<TrabajadorListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<TrabajadorListDto> result = trabajadorService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Listado de trabajadores", false, result));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<TrabajadorComboDto>>> listarActivos() {
        List<TrabajadorComboDto> result = trabajadorService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Trabajadores activos", false, result));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<TrabajadorResponseDto>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoDto dto) {
        TrabajadorResponseDto result = trabajadorService.cambiarEstado(id, dto.getActivo());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Estado actualizado", false, result));
    }

    @GetMapping("/{id}/limites")
    public ResponseEntity<ApiResponse<LimiteTrabajadorResponseDto>> obtenerLimite(@PathVariable Long id) {
        LimiteTrabajadorResponseDto result = limiteTrabajadorService.obtenerPorTrabajador(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Límite del trabajador", false, result));
    }

    @PostMapping("/{id}/limites")
    public ResponseEntity<ApiResponse<LimiteTrabajadorResponseDto>> guardarLimite(
            @PathVariable Long id, @Valid @RequestBody LimiteTrabajadorRequestDto dto) {
        LimiteTrabajadorResponseDto result = limiteTrabajadorService.guardar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Límite guardado", false, result));
    }
}
