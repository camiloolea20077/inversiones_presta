package com.cloud_technological.inversiones_prestar.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.ClienteConPrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoListDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.PrestamoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionRequestDto;
import com.cloud_technological.inversiones_prestar.dto.prestamos.SimulacionResponseDto;
import com.cloud_technological.inversiones_prestar.services.PrestamoService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PrestamoResponseDto>> crear(
            @Valid @RequestBody PrestamoRequestDto dto) {
        PrestamoResponseDto result = prestamoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Préstamo creado", false, result));
    }

    @PostMapping("/cliente-nuevo")
    public ResponseEntity<ApiResponse<ClienteConPrestamoResponseDto>> crearClienteConPrestamo(
            @Valid @RequestBody ClienteConPrestamoRequestDto dto) {
        ClienteConPrestamoResponseDto result = prestamoService.crearClienteConPrestamo(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Cliente y préstamo creados", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestamoResponseDto>> obtener(@PathVariable Long id) {
        PrestamoResponseDto result = prestamoService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Préstamo encontrado", false, result));
    }

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<PrestamoListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<PrestamoListDto> result = prestamoService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Listado de préstamos", false, result));
    }

    @PostMapping("/simular")
    public ResponseEntity<ApiResponse<SimulacionResponseDto>> simular(
            @Valid @RequestBody SimulacionRequestDto dto) {
        SimulacionResponseDto result = prestamoService.simular(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Simulación de préstamo", false, result));
    }
}
