package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.ProductoCreateDTO;
import com.distrimarket.commons.dto.ProductoDetailResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.mapper.ProductoMapper;
import com.distrimarket.inventario.repository.ProductoRepository;
import com.distrimarket.inventario.service.ProductoService;
import org.springframework.stereotype.Service;

@Service
public class ProductoServiceImpl
        extends BaseServiceImpl<Producto, ProductoCreateDTO, ProductoDetailResponseDTO>
        implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        super(productoRepository, productoMapper);
        this.productoRepository = productoRepository;
    }
}