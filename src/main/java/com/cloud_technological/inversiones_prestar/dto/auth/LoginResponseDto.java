package com.cloud_technological.inversiones_prestar.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String tipoToken;
    private Long usuarioId;
    private String nombreCompleto;
    private String correo;
    private String rol;
}
