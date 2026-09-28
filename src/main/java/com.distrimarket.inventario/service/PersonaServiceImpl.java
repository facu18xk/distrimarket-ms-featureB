package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.dto.TipoPersona;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.PersonaMapper;
import com.distrimarket.inventario.repository.PersonaRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.PersonaService;
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
    @Transactional(readOnly = true)
    public PersonaResponseDTO findByCi(String ci) {
        Persona persona = personaRepository.findByCi(ci)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona con el CI: " + ci));
        return mapper.toDTO(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponseDTO findByRuc(String ruc) {
        Persona persona = personaRepository.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona con el RUC: " + ruc));
        return mapper.toDTO(persona);
    }

    @Override
    @Transactional
    public PersonaResponseDTO create(PersonaRequestDTO requestDTO) {
        validateCreate(requestDTO);
        return super.create(requestDTO);
    }

    @Override
    @Transactional
    public PersonaResponseDTO update(Long id, PersonaRequestDTO requestDTO) {
        validateUpdate(id, requestDTO);
        return super.update(id, requestDTO);
    }

    private void validateCreate(PersonaRequestDTO dto) {
        if (dto.getTipoPersona() == null) {
            throw new IllegalArgumentException("El tipo de persona es obligatorio.");
        }

        if (dto.getTipoPersona() == TipoPersona.FISICA) {
            if (dto.getCi() == null || dto.getCi().isBlank()) {
                throw new IllegalArgumentException("Una persona física debe poseer un número de CI.");
            }
            if (personaRepository.existsByCi(dto.getCi())) {
                throw new DuplicateResourceException("Ya existe una persona registrada con el CI: " + dto.getCi());
            }
            // En caso de que una persona física también declare RUC
            if (dto.getRuc() != null && !dto.getRuc().isBlank() && personaRepository.existsByRuc(dto.getRuc())) {
                throw new DuplicateResourceException("Ya existe una persona registrada con el RUC: " + dto.getRuc());
            }
        } else if (dto.getTipoPersona() == TipoPersona.JURIDICA) {
            if (dto.getRuc() == null || dto.getRuc().isBlank()) {
                throw new IllegalArgumentException("Una persona jurídica debe poseer un RUC.");
            }
            if (personaRepository.existsByRuc(dto.getRuc())) {
                throw new DuplicateResourceException("Ya existe una persona jurídica con el RUC: " + dto.getRuc());
            }
        }
    }

    private void validateUpdate(Long id, PersonaRequestDTO dto) {
        // Obtenemos la persona actual antes de actualizar
        Persona actual = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra la persona con ID: " + id));

        // Validar que si cambia el CI, no colisione con otra persona
        if (dto.getCi() != null && !dto.getCi().equals(actual.getCi())) {
            if (personaRepository.existsByCi(dto.getCi())) {
                throw new DuplicateResourceException("El nuevo CI ya pertenece a otra persona: " + dto.getCi());
            }
        }

        // Validar que si cambia el RUC, no colisione con otra persona
        if (dto.getRuc() != null && !dto.getRuc().equals(actual.getRuc())) {
            if (personaRepository.existsByRuc(dto.getRuc())) {
                throw new DuplicateResourceException("El nuevo RUC ya pertenece a otra persona: " + dto.getRuc());
            }
        }
    }
}