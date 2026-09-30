package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.TransferenciaStockRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockResponseDTO;
import com.distrimarket.commons.entity.TransferenciaStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferenciaStockService extends BaseService<TransferenciaStock, TransferenciaStockRequestDTO, TransferenciaStockResponseDTO> {
    Page<TransferenciaStockResponseDTO> findAllWithSpecifications(Long origenId, Long destinoId, Long empleadoId, Pageable pageable);
}