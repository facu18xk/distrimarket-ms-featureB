package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.controller.BaseController;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Endpoints para la gestión de productos")
public class ProductoController extends BaseController<Producto, ProductoRequestDTO, ProductoResponseDTO> {

    public ProductoController(ProductoService productoService) {
        super(productoService);
    }
}