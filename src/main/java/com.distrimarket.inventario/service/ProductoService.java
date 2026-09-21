package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.ProductoCreateDTO;
import com.distrimarket.commons.dto.ProductoDetailResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.BaseService;

public interface ProductoService extends BaseService<Producto, ProductoCreateDTO, ProductoDetailResponseDTO> {
}