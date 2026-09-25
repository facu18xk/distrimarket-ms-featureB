package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.inventario.mapper.MarcaMapper;
import com.distrimarket.inventario.repository.MarcaRepository;
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
    public MarcaResponseDTO findById(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        return super.findById(id);
    }

    @Override
    @Transactional
    public MarcaResponseDTO create(MarcaRequestDTO createDTO) {
        if (marcaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public MarcaResponseDTO update(Long id, MarcaRequestDTO createDTO) {
        if (!marcaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        super.deleteById(id);
    }
}