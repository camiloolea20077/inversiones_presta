package com.cloud_technological.inversiones_prestar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.presupuesto.AbrirPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.MovimientoPresupuestoRequestDto;
import com.cloud_technological.inversiones_prestar.dto.presupuesto.PresupuestoResponseDto;
import com.cloud_technological.inversiones_prestar.services.PresupuestoService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/presupuesto")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    /** Presupuesto activo del administrador (o null si aún no se ha abierto). */
    @GetMapping
    public ResponseEntity<ApiResponse<PresupuestoResponseDto>> obtenerActivo() {
        PresupuestoResponseDto result = presupuestoService.obtenerActivo();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Presupuesto del administrador", false, result));
    }

    @PostMapping("/abrir")
    public ResponseEntity<ApiResponse<PresupuestoResponseDto>> abrir(
            @Valid @RequestBody AbrirPresupuestoRequestDto dto) {
        PresupuestoResponseDto result = presupuestoService.abrir(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Presupuesto inicial registrado", false, result));
    }

    @PostMapping("/capitalizar")
    public ResponseEntity<ApiResponse<PresupuestoResponseDto>> capitalizar(
            @Valid @RequestBody MovimientoPresupuestoRequestDto dto) {
        PresupuestoResponseDto result = presupuestoService.capitalizar(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Capitalización registrada", false, result));
    }

    @PostMapping("/retirar")
    public ResponseEntity<ApiResponse<PresupuestoResponseDto>> retirar(
            @Valid @RequestBody MovimientoPresupuestoRequestDto dto) {
        PresupuestoResponseDto result = presupuestoService.retirar(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Retiro registrado", false, result));
    }
}
