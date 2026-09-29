package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.enums.TipoPersona;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.EmpleadoMapper;
import com.distrimarket.inventario.repository.EmpleadoRepository;
import com.distrimarket.inventario.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoServiceImpl
        extends BaseServiceImpl<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO>
        implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               EmpleadoMapper empleadoMapper,
                               PersonaRepository personaRepository) {
        super(empleadoRepository, empleadoMapper);
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO create(EmpleadoRequestDTO createDTO) {
        Persona persona = validatePersona(createDTO.getIdPersona());

        if (empleadoRepository.existsByPersonaId(createDTO.getIdPersona())) {
            throw new DuplicateResourceException("La persona con ID " + createDTO.getIdPersona() + " ya está registrada como empleado.");
        }

        Empleado entity = mapper.toEntity(createDTO);
        entity.setPersona(persona);

        Empleado saved = empleadoRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO update(Long id, EmpleadoRequestDTO updateDTO) {
        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        Persona persona = validatePersona(updateDTO.getIdPersona());

        if (empleadoRepository.existsByPersonaIdAndIdNot(updateDTO.getIdPersona(), id)) {
            throw new DuplicateResourceException("La persona con ID " + updateDTO.getIdPersona() + " ya está asociada a otro empleado.");
        }

        mapper.updateEntityFromDto(updateDTO, empleadoExistente);
        empleadoExistente.setPersona(persona);

        Empleado updated = empleadoRepository.save(empleadoExistente);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO findByPersonaId(Long personaId) {
        Empleado empleado = empleadoRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún empleado asociado a la persona con ID: " + personaId));
        return mapper.toDTO(empleado);
    }

    private Persona validatePersona(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona registrada con ID: " + personaId));

        if (persona.getTipoPersona() != TipoPersona.FISICA) {
            throw new IllegalArgumentException("Un empleado debe ser obligatoriamente una persona física (TipoPersona.FISICA).");
        }

        return persona;
    }
}