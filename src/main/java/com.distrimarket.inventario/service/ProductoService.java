package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.BaseService;

public interface ProductoService extends BaseService<Producto, ProductoRequestDTO, ProductoResponseDTO> {
}