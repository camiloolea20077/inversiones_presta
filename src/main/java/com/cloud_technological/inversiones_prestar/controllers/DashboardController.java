package com.cloud_technological.inversiones_prestar.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.dashboard.DashboardResumenDto;
import com.cloud_technological.inversiones_prestar.services.DashboardService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResumenDto>> resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        DashboardResumenDto result = dashboardService.resumen(fecha);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Resumen del dashboard administrativo", false, result));
    }
}
