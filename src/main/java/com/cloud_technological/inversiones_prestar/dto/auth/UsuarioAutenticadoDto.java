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
public class UsuarioAutenticadoDto {

    private Long usuarioId;
    private String nombreCompleto;
    private String correo;
    private String rol;
}
