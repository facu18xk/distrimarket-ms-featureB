package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.inventario.mapper.CategoriaMapper;
import com.distrimarket.inventario.repository.CategoriaRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl extends BaseServiceImpl<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        super(categoriaRepository, categoriaMapper);
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaResponseDTO findById(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        return super.findById(id);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO createDTO) {
        if (categoriaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO createDTO) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("No existe una categoría con el ID: " + id);
        }
        super.deleteById(id);
    }
}