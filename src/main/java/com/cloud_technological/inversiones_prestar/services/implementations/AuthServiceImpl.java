package com.cloud_technological.inversiones_prestar.services.implementations;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cloud_technological.inversiones_prestar.dto.auth.LoginRequestDto;
import com.cloud_technological.inversiones_prestar.dto.auth.LoginResponseDto;
import com.cloud_technological.inversiones_prestar.dto.auth.UsuarioAutenticadoDto;
import com.cloud_technological.inversiones_prestar.entity.UsuarioEntity;
import com.cloud_technological.inversiones_prestar.repositories.usuarios.UsuarioJPARepository;
import com.cloud_technological.inversiones_prestar.security.JwtTokenProvider;
import com.cloud_technological.inversiones_prestar.services.AuthService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioJPARepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponseDto login(LoginRequestDto loginDto) {
        try {
            // 1. Autenticar credenciales (valida correo y contraseña hasheada)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getCorreo(), loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 2. Cargar el usuario para construir la respuesta
            UsuarioEntity usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(loginDto.getCorreo())
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

            if (Boolean.FALSE.equals(usuario.getActivo())) {
                throw new GlobalException(HttpStatus.UNAUTHORIZED, "El usuario está inactivo");
            }

            String rol = usuario.getRol().getNombre();

            // 3. Validar que el rol seleccionado en pantalla coincida con el del usuario
            if (loginDto.getRol() != null && !loginDto.getRol().isBlank()
                    && !rol.equalsIgnoreCase(loginDto.getRol())) {
                throw new GlobalException(HttpStatus.FORBIDDEN,
                        "El usuario no tiene el rol seleccionado");
            }

            // 4. Generar token
            String token = jwtTokenProvider.generateToken(authentication, usuario.getId(), rol);

            return LoginResponseDto.builder()
                    .token(token)
                    .tipoToken("Bearer")
                    .usuarioId(usuario.getId())
                    .nombreCompleto(usuario.getNombre())
                    .correo(usuario.getEmail())
                    .rol(rol)
                    .build();

        } catch (BadCredentialsException e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en el proceso de login");
        }
    }

    @Override
    public UsuarioAutenticadoDto obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado");
        }

        UsuarioEntity usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(authentication.getName())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return UsuarioAutenticadoDto.builder()
                .usuarioId(usuario.getId())
                .nombreCompleto(usuario.getNombre())
                .correo(usuario.getEmail())
                .rol(usuario.getRol().getNombre())
                .build();
    }
}
