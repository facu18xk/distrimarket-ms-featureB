package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepositoService extends BaseService<Deposito, DepositoRequestDTO, DepositoResponseDTO> {
    Page<DepositoResponseDTO> findAllWithSpecifications(String q, Pageable pageable);
}
