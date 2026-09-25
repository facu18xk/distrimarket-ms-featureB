package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;

public interface DepositoService extends BaseService<Deposito, DepositoRequestDTO, DepositoResponseDTO> {
    DepositoResponseDTO findById(Long id);
    DepositoResponseDTO create(DepositoRequestDTO createDTO);
    DepositoResponseDTO update(Long id, DepositoRequestDTO createDTO);
    void deleteById(Long id);
}
