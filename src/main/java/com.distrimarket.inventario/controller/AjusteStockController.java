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

    /*@GetMapping
    @Operation(summary = "Listar ajustes de stock con paginación")
    public ResponseEntity<Page<AjusteStockResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {
        return doGetAll(pageable);
    }*/

    @GetMapping
    @Operation(summary = "Listar ajustes de stock con paginación y filtros opcionales por depósito o empleado")
    public ResponseEntity<Page<AjusteStockResponseDTO>> getAll(
            @Parameter(description = "ID del depósito a filtrar (opcional)")
            @RequestParam(name = "depositoId", required = false) Long depositoId,
            @Parameter(description = "ID del empleado a filtrar (opcional)")
            @RequestParam(name = "empleadoId", required = false) Long empleadoId,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        return ResponseEntity.ok(ajusteStockService.findAllWithSpecifications(depositoId, empleadoId, pageable));
    }

    @Override
    @PostMapping
    @Operation(summary = "Registrar un nuevo ajuste de stock e impactar el inventario")
    public ResponseEntity<AjusteStockResponseDTO> create(@Valid @RequestBody AjusteStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ajusteStockService.create(createDTO));
    }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Operación no soportada (inmutable)")
    public ResponseEntity<AjusteStockResponseDTO> update(@PathVariable Long id, @Valid @RequestBody AjusteStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Operación no soportada (inmutable)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}