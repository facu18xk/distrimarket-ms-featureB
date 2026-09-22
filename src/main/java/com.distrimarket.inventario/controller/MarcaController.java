package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.inventario.service.MarcaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/marcas")
@Tag(name = "Marcas", description = "Endpoints para la gestión de marcas de productos")
public class MarcaController extends BaseController<Marca, MarcaRequestDTO, MarcaResponseDTO> {

    public MarcaController(MarcaService marcaService) {
        super(marcaService);
    }
}