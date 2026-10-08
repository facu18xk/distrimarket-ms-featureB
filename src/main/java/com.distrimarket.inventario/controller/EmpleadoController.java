package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.inventario.service.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empleados")
@Tag(name = "Empleados", description = "Endpoints para la gestión de empleados del supermercado")
public class EmpleadoController extends BaseController<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        super(empleadoService);
        this.empleadoService = empleadoService;
    }

    @GetMapping
    @Operation(summary = "Listar empleados con paginación y filtros opcionales por cargo o estado")
    public ResponseEntity<Page<EmpleadoResponseDTO>> getAll(
            @Parameter(description = "Cargo del empleado (opcional)")
            @RequestParam(name = "cargo", required = false) String cargo,
            @Parameter(description = "Estado del empleado (opcional)")
            @RequestParam(name = "estado", required = false) Boolean estado,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(empleadoService.findAllWithSpecifications(cargo, estado, pageable));
    }

    @GetMapping("/persona/{personaId}")
    @Operation(summary = "Buscar ficha de empleado asociada a un ID de persona")
    public ResponseEntity<EmpleadoResponseDTO> getByPersonaId(@PathVariable Long personaId) {
        return ResponseEntity.ok(empleadoService.findByPersonaId(personaId));
    }
}