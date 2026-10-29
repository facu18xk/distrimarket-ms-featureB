package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.inventario.service.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/empleados")
@Tag(name = "Empleados", description = "Endpoints para la gestión de empleados del supermercado")
public class EmpleadoController extends BaseController<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        super(empleadoService);
        this.empleadoService = empleadoService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'DEPOSITERO')")
    @Operation(summary = "Obtener un empleado por su ID")
    public ResponseEntity<EmpleadoResponseDTO> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'DEPOSITERO')")
    @Operation(summary = "Listar empleados con paginación y filtros opcionales por cargo o estado")
    public ResponseEntity<Page<EmpleadoResponseDTO>> getAll(
            @Parameter(description = "Cargo del empleado (opcional)")
            @RequestParam(name = "cargo", required = false) String cargo,
            @Parameter(description = "Estado del empleado (opcional)")
            @RequestParam(name = "estado", required = false) Boolean estado,
            @Parameter(description = "Texto a buscar (opcional)")
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(empleadoService.findAllWithSpecifications(cargo, estado, q, pageable));
    }

    @GetMapping("/persona/{personaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar ficha de empleado asociada a un ID de persona")
    public ResponseEntity<EmpleadoResponseDTO> getByPersonaId(@PathVariable Long personaId) {
        return ResponseEntity.ok(empleadoService.findByPersonaId(personaId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo empleado")
    public ResponseEntity<EmpleadoResponseDTO> create(@Valid @RequestBody EmpleadoRequestDTO createDTO) {
        return super.create(createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar los datos de un empleado")
    public ResponseEntity<EmpleadoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EmpleadoRequestDTO createDTO) {
        return super.update(id, createDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar o dar de baja un empleado")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar un empleado previamente dado de baja")
    public ResponseEntity<Void> reactivar(@PathVariable Long id) {
        return super.reactivar(id);
    }
}
