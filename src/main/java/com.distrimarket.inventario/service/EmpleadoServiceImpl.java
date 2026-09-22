package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.inventario.exception.BadRequestException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.EmpleadoMapper;
import com.distrimarket.inventario.repository.EmpleadoRepository;
import com.distrimarket.inventario.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoServiceImpl extends BaseServiceImpl<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final EmpleadoMapper empleadoMapper;

    public EmpleadoServiceImpl(
            EmpleadoRepository empleadoRepository,
            PersonaRepository personaRepository,
            EmpleadoMapper empleadoMapper) {
        super(empleadoRepository, empleadoMapper);
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.empleadoMapper = empleadoMapper;
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO create(EmpleadoRequestDTO createDTO) {
        // 1. Validar existencia de la Persona
        Persona persona = buscarPersonaPorId(createDTO.getIdPersona());

        // 2. Validar que la persona no esté registrada como otro empleado
        if (empleadoRepository.existsByPersonaId(createDTO.getIdPersona())) {
            throw new BadRequestException("La persona seleccionada ya está registrada como un empleado.");
        }

        // 3. Mapear y vincular relación Persona
        Empleado empleado = empleadoMapper.toEntity(createDTO);
        empleado.setPersona(persona);

        Empleado savedEntity = empleadoRepository.save(empleado);
        return empleadoMapper.toDTO(savedEntity);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO update(Long id, EmpleadoRequestDTO updateDTO) {
        // 1. Verificar existencia del Empleado
        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        // 2. Validar existencia de la nueva Persona
        Persona persona = buscarPersonaPorId(updateDTO.getIdPersona());

        // 3. Verificar que la persona no pertenezca a OTRO empleado distinto al actual
        if (empleadoRepository.existsByPersonaIdAndIdNot(updateDTO.getIdPersona(), id)) {
            throw new BadRequestException("La persona seleccionada ya está asignada a otro empleado.");
        }

        // 4. Actualizar campos
        empleadoMapper.updateEntityFromDTO(updateDTO, empleadoExistente);
        empleadoExistente.setPersona(persona);

        Empleado updatedEntity = empleadoRepository.save(empleadoExistente);
        return empleadoMapper.toDTO(updatedEntity);
    }

    private Persona buscarPersonaPorId(Long personaId) {
        return personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + personaId));
    }
}