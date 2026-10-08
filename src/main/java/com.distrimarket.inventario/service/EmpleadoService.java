package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmpleadoService extends BaseService<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {
    EmpleadoResponseDTO findByPersonaId(Long personaId);
    Page<EmpleadoResponseDTO> findAllWithSpecifications(String cargo, Boolean estado, Pageable pageable);
}