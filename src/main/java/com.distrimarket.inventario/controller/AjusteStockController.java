package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.inventario.service.AjusteStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ajustes-stock")
@Tag(name = "Ajustes Stock", description = "Endpoints para la gestión e historial de ajustes de stock")
public class AjusteStockController extends BaseController<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> {

    public AjusteStockController(AjusteStockService service) {
        super(service);
    }

    // Deshabilitamos PUT y DELETE para garantizar la integridad auditables del inventario
    @Override
    public ResponseEntity update(Long id, AjusteStockRequestDTO createDTO) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity delete(Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}