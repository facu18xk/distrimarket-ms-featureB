package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.MarcaMapper;
import com.distrimarket.inventario.repository.MarcaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MarcaServiceImpl extends BaseServiceImpl<Marca, MarcaRequestDTO, MarcaResponseDTO> implements MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaServiceImpl(MarcaRepository marcaRepository, MarcaMapper marcaMapper) {
        super(marcaRepository, marcaMapper);
        this.marcaRepository = marcaRepository;
    }

    @Override
    @Transactional
    public MarcaResponseDTO create(MarcaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de marca: '{}'", createDTO.getNombre());
        if (marcaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe una marca con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public MarcaResponseDTO update(Long id, MarcaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para marca ID {}: '{}'", id, createDTO.getNombre());
        if (marcaRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otra marca con el nombre: " + createDTO.getNombre());
        }
        return super.update(id, createDTO);
    }
}