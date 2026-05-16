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

import com.cloud_technological.inversiones_prestar.dto.clientes.CambiarEstadoClienteDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteComboDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteListDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteRequestDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ClienteResponseDto;
import com.cloud_technological.inversiones_prestar.services.ClienteService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponseDto>> crear(
            @Valid @RequestBody ClienteRequestDto dto) {
        ClienteResponseDto result = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Cliente creado", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> actualizar(
            @PathVariable Long id, @Valid @RequestBody ClienteRequestDto dto) {
        ClienteResponseDto result = clienteService.actualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cliente actualizado", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> obtener(@PathVariable Long id) {
        ClienteResponseDto result = clienteService.obtener(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cliente encontrado", false, result));
    }

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<ClienteListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<ClienteListDto> result = clienteService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Listado de clientes", false, result));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ClienteComboDto>>> listarActivos() {
        List<ClienteComboDto> result = clienteService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Clientes activos", false, result));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<ClienteResponseDto>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoClienteDto dto) {
        ClienteResponseDto result = clienteService.cambiarEstado(id, dto.getEstado());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Estado actualizado", false, result));
    }
}
