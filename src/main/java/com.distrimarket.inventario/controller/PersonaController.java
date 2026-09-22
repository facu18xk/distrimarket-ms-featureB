package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.inventario.service.PersonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personas")
@Tag(name = "Personas", description = "Endpoints para la gestión de personas")
public class PersonaController extends BaseController<Persona, PersonaRequestDTO, PersonaResponseDTO> {

    public PersonaController(PersonaService personaService) {
        super(personaService);
    }
}