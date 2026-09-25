package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.inventario.service.DepositoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/depositos")
@Tag(name = "Depósitos", description = "Endpoints para la gestión de depósitos de productos")
public class DepositoController extends BaseController<Deposito, DepositoRequestDTO, DepositoResponseDTO> {

    public DepositoController(DepositoService depositoService) {
        super(depositoService);
    }
}