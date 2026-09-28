package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.StockDeposito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockDepositoService extends BaseService<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {
    Page<StockDepositoResponseDTO> findAllWithSpecifications(Long depositoId, Long productoId, Pageable pageable);
    StockDepositoResponseDTO findByDepositoIdAndProductoId(Long depositoId, Long productoId);
}