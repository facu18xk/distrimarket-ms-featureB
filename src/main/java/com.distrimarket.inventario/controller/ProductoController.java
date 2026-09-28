package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Endpoints para la gestión de productos")
public class ProductoController extends BaseController<Producto, ProductoRequestDTO, ProductoResponseDTO> {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        super(productoService);
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos con paginación y filtros opcionales por categoría o marca")
    public ResponseEntity<Page<ProductoResponseDTO>> getAll(
            @Parameter(description = "ID de la categoría a filtrar (opcional)")
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @Parameter(description = "ID de la marca a filtrar (opcional)")
            @RequestParam(name = "marcaId", required = false) Long marcaId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(productoService.findAllWithSpecifications(categoriaId, marcaId, pageable));
    }
}