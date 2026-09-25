package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService extends BaseService<Producto, ProductoRequestDTO, ProductoResponseDTO> {
    ProductoResponseDTO findById(Long id);
    ProductoResponseDTO create(ProductoRequestDTO createDTO);
    ProductoResponseDTO update(Long id, ProductoRequestDTO createDTO);
    void deleteById(Long id);
    Page<ProductoResponseDTO> findAllWithSpecifications(Long categoriaId, Long marcaId, Pageable pageable);

}
