package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServiceImpl extends BaseServiceImpl<Producto, ProductoRequestDTO, ProductoResponseDTO> implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        super(productoRepository, productoMapper);
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public ProductoResponseDTO findById(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No existe un producto con el ID: " + id);
        }
        return super.findById(id);
    }

    @Override
    @Transactional
    public ProductoResponseDTO create(ProductoRequestDTO createDTO) {
        if (productoRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public ProductoResponseDTO update(Long id, ProductoRequestDTO createDTO) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No existe un producto con el ID: " + id);
        }
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No existe un producto con el ID: " + id);
        }
        super.deleteById(id);
    }
}