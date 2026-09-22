package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.enums.TipoPersona;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.PersonaMapper;
import com.distrimarket.inventario.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, PersonaRequestDTO, PersonaResponseDTO> implements PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository, PersonaMapper personaMapper) {
        super(personaRepository, personaMapper);
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional
    public PersonaResponseDTO create(PersonaRequestDTO createDTO) {
        validarUnicidadDocumentos(createDTO, null);
        return super.create(createDTO);
    }

    @Override
    @Transactional
    public PersonaResponseDTO update(Long id, PersonaRequestDTO updateDTO) {
        if (!personaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Persona no encontrada con ID: " + id);
        }
        validarUnicidadDocumentos(updateDTO, id);
        return super.update(id, updateDTO);
    }

    private void validarUnicidadDocumentos(PersonaRequestDTO dto, Long currentId) {
        Long idParaComparar = (currentId != null) ? currentId : -1L;
        boolean esFisica = dto.getTipoPersona() != null
                && TipoPersona.FISICA.name().equals(dto.getTipoPersona().toString());

        // 1. Validar C.I.
        if (esFisica && dto.getCi() != null && !dto.getCi().trim().isEmpty()) {
            if (personaRepository.existsByCiAndIdNot(dto.getCi().trim(), idParaComparar)) {
                throw new BadRequestException("Ya existe una persona registrada con la C.I. Nº: " + dto.getCi());
            }
        }

        // 2. Validar R.U.C.
        if (dto.getRuc() != null && !dto.getRuc().trim().isEmpty()) {
            if (personaRepository.existsByRucAndIdNot(dto.getRuc().trim(), idParaComparar)) {
                throw new BadRequestException("Ya existe una persona registrada con el R.U.C. Nº: " + dto.getRuc());
            }
        }
    }
}