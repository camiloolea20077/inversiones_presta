package com.cloud_technological.inversiones_prestar.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.clientes.InsertarClienteRutaDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.ReordenarRutaDto;
import com.cloud_technological.inversiones_prestar.dto.clientes.RutaClienteListDto;
import com.cloud_technological.inversiones_prestar.services.RutaClienteService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rutas/{rutaId}/clientes")
@RequiredArgsConstructor
public class RutaClienteController {

    private final RutaClienteService rutaClienteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RutaClienteListDto>>> listar(@PathVariable Long rutaId) {
        List<RutaClienteListDto> result = rutaClienteService.listarPorRuta(rutaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Clientes de la ruta", false, result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<RutaClienteListDto>>> insertar(
            @PathVariable Long rutaId, @Valid @RequestBody InsertarClienteRutaDto dto) {
        List<RutaClienteListDto> result = rutaClienteService.insertarEnRuta(
                rutaId, dto.getClienteId(), dto.getOrdenBase());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Cliente insertado en la ruta", false, result));
    }

    @PutMapping("/reordenar")
    public ResponseEntity<ApiResponse<List<RutaClienteListDto>>> reordenar(
            @PathVariable Long rutaId, @Valid @RequestBody ReordenarRutaDto dto) {
        List<RutaClienteListDto> result = rutaClienteService.reordenar(rutaId, dto.getOrdenIds());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Orden actualizado", false, result));
    }

    @DeleteMapping("/{rutaClienteId}")
    public ResponseEntity<ApiResponse<List<RutaClienteListDto>>> quitar(
            @PathVariable Long rutaId, @PathVariable Long rutaClienteId) {
        List<RutaClienteListDto> result = rutaClienteService.quitarDeRuta(rutaClienteId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Cliente retirado de la ruta", false, result));
    }
}
