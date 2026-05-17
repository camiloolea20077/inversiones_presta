package com.cloud_technological.inversiones_prestar.services.implementations;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.mora.MoraListDto;
import com.cloud_technological.inversiones_prestar.repositories.mora.MoraQueryRepository;
import com.cloud_technological.inversiones_prestar.services.MoraService;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;

import lombok.RequiredArgsConstructor;

/**
 * Reporte de clientes en mora (HU-BE-019). Reúsa la misma definición de mora
 * del dashboard (HU-BE-018): préstamos activos con cuotas vencidas y saldo.
 */
@Service
@RequiredArgsConstructor
public class MoraServiceImpl implements MoraService {

    private final MoraQueryRepository moraQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<MoraListDto> listar(PageableDto<Object> pageable) {
        return moraQueryRepository.listar(pageable);
    }
}
