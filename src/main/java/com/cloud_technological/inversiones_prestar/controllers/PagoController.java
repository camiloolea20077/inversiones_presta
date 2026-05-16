package com.cloud_technological.inversiones_prestar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.pagos.MarcarNoPagoDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.RegistrarPagoDto;
import com.cloud_technological.inversiones_prestar.services.PagoService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponseDto>> registrar(
            @Valid @RequestBody RegistrarPagoDto dto) {
        PagoResponseDto result = pagoService.registrarPago(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Pago registrado", false, result));
    }

    @PostMapping("/no-pago")
    public ResponseEntity<ApiResponse<PagoResponseDto>> marcarNoPago(
            @Valid @RequestBody MarcarNoPagoDto dto) {
        PagoResponseDto result = pagoService.marcarNoPago(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Cliente marcado como no pagó", false, result));
    }
}
