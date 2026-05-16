package com.cloud_technological.inversiones_prestar.services;

import com.cloud_technological.inversiones_prestar.dto.auth.LoginRequestDto;
import com.cloud_technological.inversiones_prestar.dto.auth.LoginResponseDto;
import com.cloud_technological.inversiones_prestar.dto.auth.UsuarioAutenticadoDto;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto loginDto);

    UsuarioAutenticadoDto obtenerUsuarioAutenticado();
}
