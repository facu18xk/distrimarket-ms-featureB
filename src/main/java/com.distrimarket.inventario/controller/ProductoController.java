package com.distrimarket.inventario.controller;

import com.distrimarket.inventario.controller.BaseController;
import com.distrimarket.commons.dto.ProductoCreateDTO;
import com.distrimarket.commons.dto.ProductoDetailResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.ProductoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
public class ProductoController extends BaseController<Producto, ProductoCreateDTO, ProductoDetailResponseDTO> {

    public ProductoController(ProductoService productoService) {
        super(productoService);
    }
}