package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.commons.entity.Marca;

public interface MarcaService extends BaseService<Marca, MarcaRequestDTO, MarcaResponseDTO> {
    MarcaResponseDTO findById(Long id);
    MarcaResponseDTO create(MarcaRequestDTO createDTO);
    MarcaResponseDTO update(Long id, MarcaRequestDTO createDTO);
    void deleteById(Long id);
}
