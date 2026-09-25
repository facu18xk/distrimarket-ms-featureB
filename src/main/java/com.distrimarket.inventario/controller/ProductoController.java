package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Override
    @GetMapping
    public ResponseEntity<Page<ProductoResponseDTO>> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
            /*@RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "marcaId", required = false) Long marcaId*/) {

        Long categoriaId = getLongParameter("categoriaId");
        Long marcaId = getLongParameter("marcaId");
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductoResponseDTO> pagedResult = productoService.findAllWithSpecifications(categoriaId, marcaId, pageable);

        return ResponseEntity.ok(pagedResult);
    }

    private Long getLongParameter(String name) {
        jakarta.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                        org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();

        String value = request.getParameter(name);
        return (value != null && !value.isEmpty()) ? Long.valueOf(value) : null;
    }
}