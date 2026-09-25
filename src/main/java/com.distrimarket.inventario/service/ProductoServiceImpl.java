package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.ProductoService;
import com.distrimarket.inventario.specification.ProductoSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ProductoServiceImpl extends BaseServiceImpl<Producto, ProductoRequestDTO, ProductoResponseDTO> implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final ProductoSpecification productoSpecification;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper, ProductoSpecification productoSpecification) {
        super(productoRepository, productoMapper);
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.productoSpecification = productoSpecification;
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

    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> findAllWithSpecifications(Long categoriaId, Long marcaId, Pageable pageable) {

        Specification<Producto> spec = Specification
                .where(productoSpecification.hasCategoriaId(categoriaId))
                .and(productoSpecification.hasMarcaId(marcaId));

        Page<Producto> entityPage = productoRepository.findAll(spec, pageable);

        return entityPage.map(productoMapper::toDTO);
    }
}