package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.DepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.DepositoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DepositoServiceImpl extends BaseServiceImpl<Deposito, DepositoRequestDTO, DepositoResponseDTO> implements DepositoService {

    private final DepositoRepository depositoRepository;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoMapper depositoMapper) {
        super(depositoRepository, depositoMapper);
        this.depositoRepository = depositoRepository;
    }

    @Override
    @Transactional
    public DepositoResponseDTO create(DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de depósito: '{}'", createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe un depósito con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public DepositoResponseDTO update(Long id, DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para depósito ID {}: '{}'", id, createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otro depósito con el nombre: " + createDTO.getNombre());
        }
        return super.update(id, createDTO);
    }
}