package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.inventario.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personas")
@Tag(name = "Personas", description = "Endpoints para la gestión de personas")
public class PersonaController extends BaseController<Persona, PersonaRequestDTO, PersonaResponseDTO> {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        super(personaService);
        this.personaService = personaService;
    }

    @GetMapping("/ci/{ci}")
    @Operation(summary = "Buscar persona por CI")
    public ResponseEntity<PersonaResponseDTO> getByCi(@PathVariable String ci) {
        return ResponseEntity.ok(personaService.findByCi(ci));
    }

    @GetMapping("/ruc/{ruc}")
    @Operation(summary = "Buscar persona por RUC")
    public ResponseEntity<PersonaResponseDTO> getByRuc(@PathVariable String ruc) {
        return ResponseEntity.ok(personaService.findByRuc(ruc));
    }

    @GetMapping
    public ResponseEntity<Page<PersonaResponseDTO>> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return doGetAll(pageable);
    }
}