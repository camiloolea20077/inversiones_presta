package com.cloud_technological.inversiones_prestar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_technological.inversiones_prestar.dto.auth.LoginRequestDto;
import com.cloud_technological.inversiones_prestar.dto.auth.LoginResponseDto;
import com.cloud_technological.inversiones_prestar.dto.auth.UsuarioAutenticadoDto;
import com.cloud_technological.inversiones_prestar.services.AuthService;
import com.cloud_technological.inversiones_prestar.utils.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto result = authService.login(dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Login exitoso", false, result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioAutenticadoDto>> me() {
        UsuarioAutenticadoDto result = authService.obtenerUsuarioAutenticado();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuario autenticado", false, result));
    }
}
