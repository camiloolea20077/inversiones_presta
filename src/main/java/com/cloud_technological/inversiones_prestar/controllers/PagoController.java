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

import com.cloud_technological.inversiones_prestar.dto.pagos.MarcarNoPagoDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoDetalleDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoListDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.PagoResponseDto;
import com.cloud_technological.inversiones_prestar.dto.pagos.RegistrarPagoDto;
import com.cloud_technological.inversiones_prestar.services.PagoService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

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

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<PagoListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<PagoListDto> result = pagoService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Listado de pagos", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoDetalleDto>> obtener(@PathVariable Long id) {
        PagoDetalleDto result = pagoService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Detalle del pago", false, result));
    }
}
