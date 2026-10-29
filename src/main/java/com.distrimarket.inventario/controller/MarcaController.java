package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.commons.entity.Marca;
import com.distrimarket.inventario.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marcas")
@Tag(name = "Marcas", description = "Endpoints para la gestión de marcas de productos")
public class MarcaController extends BaseController<Marca, MarcaRequestDTO, MarcaResponseDTO> {

    public MarcaController(MarcaService marcaService) {
        super(marcaService);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener una marca por su ID")
    public ResponseEntity<MarcaResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Listar todas las marcas de forma paginada")
    public ResponseEntity<Page<MarcaResponseDTO>> getAll(
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTexto(q, pageable, "nombre"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva marca")
    public ResponseEntity<MarcaResponseDTO> create(@Valid @RequestBody MarcaRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar los datos de una marca")
    public ResponseEntity<MarcaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MarcaRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar o dar de baja una marca")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar una marca previamente dada de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}