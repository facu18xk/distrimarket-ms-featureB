package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.enums.TipoPersona;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.PersonaMapper;
import com.distrimarket.inventario.mapper.EmpleadoMapper;
import com.distrimarket.inventario.repository.EmpleadoRepository;
import com.distrimarket.inventario.repository.PersonaRepository;
import com.distrimarket.inventario.specification.EmpleadoSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmpleadoServiceImpl
        extends BaseServiceImpl<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO>
        implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final PersonaService personaService;
    private final PersonaMapper personaMapper;
    private final EmpleadoSpecification empleadoSpecification;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               EmpleadoMapper empleadoMapper,
                               PersonaRepository personaRepository,
                               PersonaService personaService,
                               PersonaMapper personaMapper,
                               EmpleadoSpecification empleadoSpecification) {
        super(empleadoRepository, empleadoMapper);
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaMapper = personaMapper;
        this.empleadoSpecification = empleadoSpecification;
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO create(EmpleadoRequestDTO createDTO) {
        log.info("Iniciando creación de empleado con cargo: '{}'", createDTO.getCargo());

        if (createDTO.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de la persona son obligatorios.");
        }

        if (createDTO.getPersona().getTipoPersona() != TipoPersona.FISICA) {
            throw new IllegalArgumentException("Un empleado debe ser una persona física (TipoPersona.FISICA).");
        }

        // 1. Resolver la persona: buscar si ya existe por CI o crearla mediante PersonaService
        String ci = createDTO.getPersona().getCi();
        Persona persona;

        if (ci != null && !ci.isBlank() && personaRepository.existsByCi(ci)) {
            log.debug("La persona con CI {} ya existe. Vinculando ficha de empleado...", ci);
            persona = personaRepository.findByCi(ci)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró la persona con CI: " + ci));
        } else {
            log.debug("Registrando nueva persona física para el empleado: {}", createDTO.getPersona().getNombreCompleto());
            PersonaResponseDTO personaCreadaDTO = personaService.create(createDTO.getPersona());
            persona = personaRepository.findById(personaCreadaDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Error al recuperar la persona creada"));
        }

        // 2. Verificar que la persona no esté asignada a otro empleado
        if (empleadoRepository.existsByPersonaId(persona.getId())) {
            throw new DuplicateResourceException("La persona ya se encuentra registrada como empleado (Persona ID: " + persona.getId() + ").");
        }

        // 3. Crear y asociar la entidad Empleado
        Empleado empleado = mapper.toEntity(createDTO);
        empleado.setPersona(persona);

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado registrado exitosamente con ID: {} asociado a Persona ID: {}", guardado.getId(), persona.getId());

        return mapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO update(Long id, EmpleadoRequestDTO updateDTO) {
        log.info("Actualizando datos del empleado con ID: {}", id);

        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        if (updateDTO.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de la persona son obligatorios para actualizar.");
        }

        // 1. Actualizar los datos de la persona asociada delegando en PersonaService
        Long personaId = empleadoExistente.getPersona().getId();
        personaService.update(personaId, updateDTO.getPersona());

        // 2. Actualizar los datos propios del empleado (cargo, fechaIngreso, estado)
        mapper.updateEntityFromDto(updateDTO, empleadoExistente);

        Empleado actualizado = empleadoRepository.save(empleadoExistente);
        log.info("Empleado ID: {} y datos de Persona ID: {} actualizados exitosamente", id, personaId);

        return mapper.toDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponseDTO> findAllWithSpecifications(String cargo, Boolean estado, Pageable pageable) {
        log.debug("Consultando empleados con filtros -> cargo: {}, estado: {}, página: {}",
                cargo, estado, pageable.getPageNumber());

        Specification<Empleado> spec = Specification
                .where(empleadoSpecification.withCargo(cargo))
                .and(empleadoSpecification.withEstado(estado));

        return empleadoRepository.findAll(spec, pageable).map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO findByPersonaId(Long personaId) {
        log.debug("Buscando empleado por persona ID: {}", personaId);
        Empleado empleado = empleadoRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún empleado asociado a la persona con ID: " + personaId));
        return mapper.toDTO(empleado);
    }

    private Persona validatePersona(Long personaId) {
        log.debug("Verificando existencia y tipo de persona para ID: {}", personaId);
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona registrada con ID: " + personaId));

        if (persona.getTipoPersona() != TipoPersona.FISICA) {
            throw new IllegalArgumentException("Un empleado debe ser obligatoriamente una persona física (TipoPersona.FISICA).");
        }

        return persona;
    }
}