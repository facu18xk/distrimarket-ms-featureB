package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import com.distrimarket.inventario.service.AjusteStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ajustes-stock")
@Tag(name = "Ajustes de Stock", description = "Endpoints para la ejecución y consulta de ajustes de inventario")
public class AjusteStockController extends BaseController<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> {

    private final AjusteStockService ajusteStockService;

    public AjusteStockController(AjusteStockService ajusteStockService) {
        super(ajusteStockService);
        this.ajusteStockService = ajusteStockService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Obtener un ajuste de stock por su ID")
    public ResponseEntity<AjusteStockResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Listar ajustes de stock con paginación y filtros opcionales por depósito o empleado")
    public ResponseEntity<Page<AjusteStockResponseDTO>> getAll(
            @Parameter(description = "ID del depósito a filtrar (opcional)")
            @RequestParam(name = "depositoId", required = false) Long depositoId,
            @Parameter(description = "ID del empleado a filtrar (opcional)")
            @RequestParam(name = "empleadoId", required = false) Long empleadoId,
            @Parameter(description = "Texto a buscar (opcional)")
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        return ResponseEntity.ok(ajusteStockService.findAllWithSpecifications(depositoId, empleadoId, q, pageable));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Registrar un nuevo ajuste de stock e impactar el inventario")
    public ResponseEntity<AjusteStockResponseDTO> create(@Valid @RequestBody AjusteStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ajusteStockService.create(createDTO));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Operación no soportada (los ajustes de stock son inmutables)")
    public ResponseEntity<AjusteStockResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AjusteStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Anular un ajuste de stock e impactar la compensación en el inventario")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ajusteStockService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Operación no soportada (los ajustes de stock no se reactivan)")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}