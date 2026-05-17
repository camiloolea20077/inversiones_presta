package com.cloud_technological.inversiones_prestar.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaListDto;
import com.cloud_technological.inversiones_prestar.dto.auditoria.AuditoriaUsuarioDto;
import com.cloud_technological.inversiones_prestar.services.AuditoriaService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import lombok.RequiredArgsConstructor;

/**
 * Auditoría general del sistema (HU-BE-020 / HU-FE-022).
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<AuditoriaListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<AuditoriaListDto> result = auditoriaService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Listado de auditoría", false, result));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<AuditoriaUsuarioDto>>> usuarios() {
        List<AuditoriaUsuarioDto> result = auditoriaService.usuarios();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Usuarios con eventos de auditoría", false, result));
    }
}
