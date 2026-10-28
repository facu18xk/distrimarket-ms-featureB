package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.inventario.service.DepositoService;
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
@RequestMapping("/depositos")
@Tag(name = "Depósitos", description = "Endpoints para la gestión de depósitos de productos")
public class DepositoController extends BaseController<Deposito, DepositoRequestDTO, DepositoResponseDTO> {

    public DepositoController(DepositoService depositoService) {
        super(depositoService);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener un depósito por su ID")
    public ResponseEntity<DepositoResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR', 'DEPOSITERO')")
    @Operation(summary = "Listar todos los depósitos de forma paginada")
    public ResponseEntity<Page<DepositoResponseDTO>> getAll(
            @org.springframework.web.bind.annotation.RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.buscarTexto(q, pageable, "nombre", "ubicacion"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo depósito")
    public ResponseEntity<DepositoResponseDTO> create(@Valid @RequestBody DepositoRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar los datos de un depósito")
    public ResponseEntity<DepositoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody DepositoRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar o dar de baja un depósito")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar un depósito previamente dado de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}