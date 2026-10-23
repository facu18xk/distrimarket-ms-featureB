package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.dto.StockMovimientoRequestDTO;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.service.StockDepositoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-depositos")
@Tag(name = "Stock en Depósitos", description = "Endpoints para la gestión y consulta de stock por depósito")
public class StockDepositoController extends BaseController<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {

    private final StockDepositoService stockDepositoService;

    public StockDepositoController(StockDepositoService stockDepositoService) {
        super(stockDepositoService);
        this.stockDepositoService = stockDepositoService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener registro de stock por ID")
    public ResponseEntity<StockDepositoResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener el stock de un producto específico en un depósito determinado")
    public ResponseEntity<StockDepositoResponseDTO> getByDepositoAndProducto(
            @PathVariable Long depositoId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(stockDepositoService.findByDepositoIdAndProductoId(depositoId, productoId));
    }

    @PostMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Aplicar movimientos de stock en un depósito (cantidad positiva ingresa, negativa descuenta)")
    public ResponseEntity<List<StockDepositoResponseDTO>> aplicarMovimientos(
            @Valid @RequestBody StockMovimientoRequestDTO movimientoDTO) {
        return ResponseEntity.ok(stockDepositoService.aplicarMovimientos(movimientoDTO));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Crear registro manual de stock en depósito")
    public ResponseEntity<StockDepositoResponseDTO> create(@Valid @RequestBody StockDepositoRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Actualizar registro de stock")
    public ResponseEntity<StockDepositoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody StockDepositoRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Eliminar o dar de baja registro de stock")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITERO')")
    @Operation(summary = "Reactivar registro de stock previamente dado de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}