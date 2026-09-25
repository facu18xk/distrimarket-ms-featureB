package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;

public interface CategoriaService extends BaseService<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {
    CategoriaResponseDTO findById(Long id);
    CategoriaResponseDTO create(CategoriaRequestDTO createDTO);
    CategoriaResponseDTO update(Long id, CategoriaRequestDTO createDTO);
    void deleteById(Long id);
}
