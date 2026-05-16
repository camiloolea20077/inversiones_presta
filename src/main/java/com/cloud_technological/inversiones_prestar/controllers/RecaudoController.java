package com.cloud_technological.inversiones_prestar.controllers;

import java.time.LocalDate;

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

import com.cloud_technological.inversiones_prestar.dto.recaudos.GenerarRecaudoDto;
import com.cloud_technological.inversiones_prestar.dto.recaudos.RecaudoDiarioResponseDto;
import com.cloud_technological.inversiones_prestar.services.RecaudoService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recaudos")
@RequiredArgsConstructor
public class RecaudoController {

    private final RecaudoService recaudoService;

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<RecaudoDiarioResponseDto>> generar(
            @Valid @RequestBody GenerarRecaudoDto dto) {
        RecaudoDiarioResponseDto result = recaudoService.generar(dto.getRutaId(), dto.getFecha());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Planilla de recaudo generada", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecaudoDiarioResponseDto>> obtener(@PathVariable Long id) {
        RecaudoDiarioResponseDto result = recaudoService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Recaudo encontrado", false, result));
    }

    @GetMapping("/mi-ruta")
    public ResponseEntity<ApiResponse<RecaudoDiarioResponseDto>> miRuta(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        RecaudoDiarioResponseDto result = recaudoService.miRutaDiaria(fecha);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Ruta diaria del trabajador", false, result));
    }
}
