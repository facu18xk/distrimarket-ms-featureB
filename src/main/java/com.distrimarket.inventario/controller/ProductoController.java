package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Producto;
import com.distrimarket.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener un producto por su ID")
    public ResponseEntity<ProductoResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Listar productos con paginación y filtros opcionales por categoría o marca")
    public ResponseEntity<Page<ProductoResponseDTO>> getAll(
            @Parameter(description = "ID de la categoría a filtrar (opcional)")
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @Parameter(description = "ID de la marca a filtrar (opcional)")
            @RequestParam(name = "marcaId", required = false) Long marcaId,
            @Parameter(description = "Texto a buscar (opcional)")
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(productoService.findAllWithSpecifications(categoriaId, marcaId, q, pageable));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Crear un nuevo producto")
    public ResponseEntity<ProductoResponseDTO> create(@Valid @RequestBody ProductoRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Actualizar datos de un producto")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Eliminar o dar de baja un producto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Reactivar un producto previamente dado de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}