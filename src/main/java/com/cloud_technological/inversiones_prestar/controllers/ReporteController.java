package com.cloud_technological.inversiones_prestar.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.reportes.RentabilidadDto;
import com.cloud_technological.inversiones_prestar.services.ReporteService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /** Reporte de rentabilidad del administrador. */
    @GetMapping("/rentabilidad")
    public ResponseEntity<ApiResponse<RentabilidadDto>> rentabilidad(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        RentabilidadDto result = reporteService.rentabilidad(fechaCorte);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Reporte de rentabilidad", false, result));
    }
}
