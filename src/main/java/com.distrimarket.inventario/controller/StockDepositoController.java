package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.StockDeposito;
import com.distrimarket.inventario.service.StockDepositoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-depositos")
@Tag(name = "Stock Depósitos", description = "Endpoints para la gestión de stock de un depósito de productos")
public class StockDepositoController extends BaseController<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {

    public StockDepositoController(StockDepositoService stockDepositoService) {
        super(stockDepositoService);
    }
}