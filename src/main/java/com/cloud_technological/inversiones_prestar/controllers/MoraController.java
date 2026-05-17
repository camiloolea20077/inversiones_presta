package com.cloud_technological.inversiones_prestar.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.mora.MoraListDto;
import com.cloud_technological.inversiones_prestar.services.MoraService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import lombok.RequiredArgsConstructor;

/**
 * Reporte de clientes en mora (HU-BE-019 / HU-FE-021).
 */
@RestController
@RequestMapping("/api/mora")
@RequiredArgsConstructor
public class MoraController {

    private final MoraService moraService;

    @PostMapping("/listar")
    public ResponseEntity<ApiResponse<Page<MoraListDto>>> listar(
            @RequestBody PageableDto<Object> pageable) {
        Page<MoraListDto> result = moraService.listar(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Reporte de clientes en mora", false, result));
    }
}
