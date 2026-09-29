package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AjusteStockService extends BaseService<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> {
    Page<AjusteStockResponseDTO> findAllWithSpecifications(Long depositoId, Long empleadoId, Pageable pageable);
    Page<AjusteStockResponseDTO> findAllByDepositoId(Long depositoId, Pageable pageable);
}