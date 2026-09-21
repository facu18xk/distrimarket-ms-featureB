package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.inventario.mapper.MarcaMapper;
import com.distrimarket.inventario.repository.MarcaRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.MarcaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Validación de duplicados por nombre
        if (marcaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new RuntimeException("Ya existe una marca con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }
}