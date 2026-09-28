package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.service.StockDepositoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-depositos")
@Tag(name = "Stock en Depósitos", description = "Endpoints para la gestión y consulta de stock por depósito")
public class StockDepositoController extends BaseController<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {

    private final StockDepositoService stockDepositoService;

    public StockDepositoController(StockDepositoService stockDepositoService) {
        super(stockDepositoService);
        this.stockDepositoService = stockDepositoService;
    }

    @GetMapping
    @Operation(summary = "Listar stock con paginación y filtros opcionales por depósito o producto")
    public ResponseEntity<Page<StockDepositoResponseDTO>> getAll(
            @Parameter(description = "ID del depósito a filtrar (opcional)")
            @RequestParam(name = "depositoId", required = false) Long depositoId,
            @Parameter(description = "ID del producto a filtrar (opcional)")
            @RequestParam(name = "productoId", required = false) Long productoId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(stockDepositoService.findAllWithSpecifications(depositoId, productoId, pageable));
    }

    @GetMapping("/deposito/{depositoId}/producto/{productoId}")
    @Operation(summary = "Obtener el stock de un producto específico en un depósito determinado")
    public ResponseEntity<StockDepositoResponseDTO> getByDepositoAndProducto(
            @PathVariable Long depositoId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(stockDepositoService.findByDepositoIdAndProductoId(depositoId, productoId));
    }
}