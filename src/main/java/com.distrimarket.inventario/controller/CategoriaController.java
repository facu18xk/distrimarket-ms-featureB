package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.inventario.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categorias")
public class CategoriaController extends BaseController<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {

    public CategoriaController(CategoriaService categoriaService) {
        super(categoriaService);
    }
}