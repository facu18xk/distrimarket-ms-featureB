package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.inventario.service.PersonaService;
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
@RequestMapping("/personas")
@Tag(name = "Personas", description = "Endpoints para la gestión de personas")
public class PersonaController extends BaseController<Persona, PersonaRequestDTO, PersonaResponseDTO> {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        super(personaService);
        this.personaService = personaService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR')")
    @Operation(summary = "Obtener una persona por su ID")
    public ResponseEntity<PersonaResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping("/ci/{ci}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR')")
    @Operation(summary = "Buscar persona por CI")
    public ResponseEntity<PersonaResponseDTO> getByCi(@PathVariable String ci) {
        return ResponseEntity.ok(personaService.findByCi(ci));
    }

    @GetMapping("/ruc/{ruc}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR')")
    @Operation(summary = "Buscar persona por RUC")
    public ResponseEntity<PersonaResponseDTO> getByRuc(@PathVariable String ruc) {
        return ResponseEntity.ok(personaService.findByRuc(ruc));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'COMPRADOR')")
    @Operation(summary = "Listar todas las personas de forma paginada")
    public ResponseEntity<Page<PersonaResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return doGetAll(pageable);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva persona")
    public ResponseEntity<PersonaResponseDTO> create(@Valid @RequestBody PersonaRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar los datos de una persona")
    public ResponseEntity<PersonaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PersonaRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar o dar de baja una persona")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar una persona previamente dada de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}