package com.distrimarket.inventario.controller;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.inventario.service.EmpleadoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/empleados")
@Tag(name = "Empleados", description = "Endpoints para la gestión de empleados")
public class EmpleadoController extends BaseController<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    public EmpleadoController(EmpleadoService empleadoService) {
        super(empleadoService);
    }
}