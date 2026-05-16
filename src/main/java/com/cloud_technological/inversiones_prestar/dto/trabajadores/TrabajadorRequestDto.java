package com.cloud_technological.inversiones_prestar.dto.trabajadores;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrabajadorRequestDto {

    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 30, message = "El documento no puede superar 30 caracteres")
    private String documento;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccion;

    /** Usuario del sistema asociado al trabajador (opcional). */
    private Long usuarioId;

    private Boolean activo;

    /* ===== Acceso al sistema (opcional) =====
       Si se diligencian, se crea/actualiza el usuario de login del trabajador. */

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String correo;

    @Size(max = 100, message = "El usuario no puede superar 100 caracteres")
    private String usuario;

    @Size(max = 100, message = "La contraseña no puede superar 100 caracteres")
    private String password;
}
