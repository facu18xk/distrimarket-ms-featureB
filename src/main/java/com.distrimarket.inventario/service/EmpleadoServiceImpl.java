package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.enums.TipoPersona;
import com.distrimarket.inventario.config.CacheConfig;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.EmpleadoMapper;
import com.distrimarket.inventario.mapper.PersonaMapper;
import com.distrimarket.inventario.repository.EmpleadoRepository;
import com.distrimarket.inventario.repository.PersonaRepository;
import com.distrimarket.inventario.specification.EmpleadoSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CacheManager cacheManager;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               EmpleadoMapper empleadoMapper,
                               PersonaRepository personaRepository,
                               PersonaService personaService,
                               PersonaMapper personaMapper,
                               EmpleadoSpecification empleadoSpecification,
                               CacheManager cacheManager) {
        super(empleadoRepository, empleadoMapper);
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.personaService = personaService;
        this.personaMapper = personaMapper;
        this.empleadoSpecification = empleadoSpecification;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.EMPLEADO, key = "#id")
    public EmpleadoResponseDTO findById(Long id) {
        log.debug("Cache miss para empleado con ID {}. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.EMPLEADO, key = "'persona:' + #personaId")
    public EmpleadoResponseDTO findByPersonaId(Long personaId) {
        log.debug("Cache miss para empleado con persona ID {}. Consultando base de datos...", personaId);
        Empleado empleado = empleadoRepository.findByPersonaId(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún empleado asociado a la persona con ID: " + personaId));
        return mapper.toDTO(empleado);
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.EMPLEADO, key = "#result.id", condition = "#result != null && #result.id != null")
    public EmpleadoResponseDTO create(EmpleadoRequestDTO createDTO) {
        log.info("Iniciando creación de empleado con cargo: '{}'", createDTO.getCargo());

        if (createDTO.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de la persona son obligatorios.");
        }

        if (createDTO.getPersona().getTipoPersona() != TipoPersona.FISICA) {
            throw new IllegalArgumentException("Un empleado debe ser una persona física (TipoPersona.FISICA).");
        }

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

        if (empleadoRepository.existsByPersonaId(persona.getId())) {
            throw new DuplicateResourceException("La persona ya se encuentra registrada como empleado (Persona ID: " + persona.getId() + ").");
        }

        Empleado empleado = mapper.toEntity(createDTO);
        empleado.setPersona(persona);

        Empleado guardado = empleadoRepository.save(empleado);
        log.info("Empleado registrado exitosamente con ID: {} asociado a Persona ID: {}", guardado.getId(), persona.getId());

        EmpleadoResponseDTO dto = mapper.toDTO(guardado);

        // Precalentar la clave secundaria persona:<personaId>
        Cache cache = cacheManager.getCache(CacheConfig.EMPLEADO);
        if (cache != null && guardado.getPersona() != null) {
            cache.put("persona:" + guardado.getPersona().getId(), dto);
        }

        return dto;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.EMPLEADO, key = "#id")
    public EmpleadoResponseDTO update(Long id, EmpleadoRequestDTO updateDTO) {
        log.info("Actualizando datos del empleado con ID: {}", id);

        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        if (updateDTO.getPersona() == null) {
            throw new IllegalArgumentException("Los datos de la persona son obligatorios para actualizar.");
        }

        Long personaId = empleadoExistente.getPersona().getId();
        // Al actualizar vía personaService, se limpian las claves de la persona en Redis
        personaService.update(personaId, updateDTO.getPersona());

        // Invalida la búsqueda por personaId si estaba en caché
        Cache cache = cacheManager.getCache(CacheConfig.EMPLEADO);
        if (cache != null) {
            cache.evict("persona:" + personaId);
        }

        mapper.updateEntityFromDto(updateDTO, empleadoExistente);
        Empleado actualizado = empleadoRepository.save(empleadoExistente);

        log.info("Empleado ID: {} y datos de Persona ID: {} actualizados exitosamente", id, personaId);
        return mapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.EMPLEADO, key = "#id")
    public void deleteById(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        Cache cache = cacheManager.getCache(CacheConfig.EMPLEADO);
        if (cache != null && empleado.getPersona() != null) {
            cache.evict("persona:" + empleado.getPersona().getId());
        }

        log.debug("Eliminando empleado ID {} y desalojando clave de Redis", id);
        super.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponseDTO> findAll(Pageable pageable) {
        Page<EmpleadoResponseDTO> page = super.findAll(pageable);
        cachearEmpleados(page);
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpleadoResponseDTO> findAllWithSpecifications(String cargo, Boolean estado, Pageable pageable) {
        log.debug("Consultando empleados con filtros -> cargo: {}, estado: {}, página: {}",
                cargo, estado, pageable.getPageNumber());

        Specification<Empleado> spec = Specification
                .where(empleadoSpecification.withCargo(cargo))
                .and(empleadoSpecification.withEstado(estado));

        Page<EmpleadoResponseDTO> dtoPage = empleadoRepository.findAll(spec, pageable).map(mapper::toDTO);
        cachearEmpleados(dtoPage);
        return dtoPage;
    }

    private void cachearEmpleados(Page<EmpleadoResponseDTO> page) {
        Cache cache = cacheManager.getCache(CacheConfig.EMPLEADO);
        if (cache != null) {
            page.forEach(dto -> {
                if (dto.getId() != null) {
                    // Guarda por ID directo
                    cache.put(dto.getId(), dto);
                    // Si el DTO expone persona o personaId, guarda también persona:<id>
                    if (dto.getPersona() != null && dto.getPersona().getId() != null) {
                        cache.put("persona:" + dto.getPersona().getId(), dto);
                    }
                }
            });
            log.debug("Se almacenaron {} empleados individualmente en caché", page.getNumberOfElements());
        }
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