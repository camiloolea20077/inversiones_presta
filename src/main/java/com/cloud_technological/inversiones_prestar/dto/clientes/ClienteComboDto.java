package com.cloud_technological.inversiones_prestar.dto.clientes;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Cliente en formato reducido para selects/combos. */
@Getter
@Setter
@Builder
public class ClienteComboDto {

    private Long id;
    private String nombre;
    private String documento;
}
