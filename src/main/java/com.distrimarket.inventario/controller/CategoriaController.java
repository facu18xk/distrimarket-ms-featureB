package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.inventario.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorías", description = "Endpoints para la gestión de categorías de productos")
public class CategoriaController extends BaseController<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {

    public CategoriaController(CategoriaService categoriaService) {
        super(categoriaService);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Listar categorías de forma paginada")
    public ResponseEntity<Page<CategoriaResponseDTO>> getAll(
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTexto(q, pageable, "nombre", "descripcion"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva categoría")
    public ResponseEntity<CategoriaResponseDTO> create(@Valid @RequestBody CategoriaRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoría existente")
    public ResponseEntity<CategoriaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar o dar de baja una categoría")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar una categoría previamente dada de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}