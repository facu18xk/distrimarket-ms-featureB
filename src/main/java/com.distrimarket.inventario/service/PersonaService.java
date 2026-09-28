package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;

public interface PersonaService extends BaseService<Persona, PersonaRequestDTO, PersonaResponseDTO> {
    PersonaResponseDTO findByCi(String ci);
    PersonaResponseDTO findByRuc(String ruc);
}
