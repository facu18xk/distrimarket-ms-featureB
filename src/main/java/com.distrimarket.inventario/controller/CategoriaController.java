package com.distrimarket.inventario.controller;

import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.inventario.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorías", description = "Endpoints para la gestión de categorías de productos")
public class CategoriaController extends BaseController<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {

    public CategoriaController(CategoriaService categoriaService) {
        super(categoriaService);
    }

    @GetMapping
    public ResponseEntity<Page<CategoriaResponseDTO>> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return doGetAll(pageable);
    }
}