package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.inventario.mapper.DepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.DepositoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositoServiceImpl extends BaseServiceImpl<Deposito, DepositoRequestDTO, DepositoResponseDTO> implements DepositoService {

    private final DepositoRepository depositoRepository;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoMapper depositoMapper) {
        super(depositoRepository, depositoMapper);
        this.depositoRepository = depositoRepository;
    }

    @Override
    @Transactional
    public DepositoResponseDTO findById(Long id) {
        if (!depositoRepository.existsById(id)) {
            throw new RuntimeException("No existe un depósito con el ID: " + id);
        }
        return super.findById(id);
    }

    @Override
    @Transactional
    public DepositoResponseDTO create(DepositoRequestDTO createDTO) {
        if (depositoRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new RuntimeException("Ya existe un depósito con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public DepositoResponseDTO update(Long id, DepositoRequestDTO createDTO) {
        if (!depositoRepository.existsById(id)) {
            throw new RuntimeException("No existe un depósito con el ID: " + id);
        }
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!depositoRepository.existsById(id)) {
            throw new RuntimeException("No existe un depósito con el ID: " + id);
        }
        super.deleteById(id);
    }
}