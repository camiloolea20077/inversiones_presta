package com.cloud_technological.inversiones_prestar.services;

import java.util.List;

import com.cloud_technological.inversiones_prestar.dto.clientes.RutaClienteListDto;

public interface RutaClienteService {

    List<RutaClienteListDto> listarPorRuta(Long rutaId);

    /** Inserta un cliente en la ruta después del orden indicado (null = al final). */
    List<RutaClienteListDto> insertarEnRuta(Long rutaId, Long clienteId, Integer ordenBase);

    /** Reasigna el orden 1..N de los clientes de la ruta. */
    List<RutaClienteListDto> reordenar(Long rutaId, List<Long> ordenIds);

    /** Retira un cliente del recorrido de la ruta. */
    List<RutaClienteListDto> quitarDeRuta(Long rutaClienteId);
}
