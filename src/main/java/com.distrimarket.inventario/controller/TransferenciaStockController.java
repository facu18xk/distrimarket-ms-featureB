package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.TransferenciaStockRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockResponseDTO;
import com.distrimarket.commons.entity.TransferenciaStock;
import com.distrimarket.inventario.service.TransferenciaStockService;
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
@RequestMapping("/transferencias-stock")
@Tag(name = "Transferencias de Stock", description = "Endpoints para la gestión y transferencia de productos entre depósitos")
public class TransferenciaStockController extends BaseController<TransferenciaStock, TransferenciaStockRequestDTO, TransferenciaStockResponseDTO> {

    private final TransferenciaStockService transferenciaStockService;

    public TransferenciaStockController(TransferenciaStockService transferenciaStockService) {
        super(transferenciaStockService);
        this.transferenciaStockService = transferenciaStockService;
    }

    @GetMapping
    @Operation(summary = "Listar transferencias con paginación y filtros opcionales")
    public ResponseEntity<Page<TransferenciaStockResponseDTO>> getAll(
            @Parameter(description = "ID del depósito de origen")
            @RequestParam(name = "depositoOrigenId", required = false) Long depositoOrigenId,
            @Parameter(description = "ID del depósito de destino")
            @RequestParam(name = "depositoDestinoId", required = false) Long depositoDestinoId,
            @Parameter(description = "ID del empleado responsable")
            @RequestParam(name = "empleadoId", required = false) Long empleadoId,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        return ResponseEntity.ok(transferenciaStockService.findAllWithSpecifications(
                depositoOrigenId, depositoDestinoId, empleadoId, pageable));
    }

    @Override
    @PostMapping
    @Operation(summary = "Registrar una nueva transferencia de stock entre depósitos")
    public ResponseEntity<TransferenciaStockResponseDTO> create(@Valid @RequestBody TransferenciaStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferenciaStockService.create(createDTO));
    }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Operación no permitida (inmutable)")
    public ResponseEntity<TransferenciaStockResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TransferenciaStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Operación no permitida (inmutable)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}