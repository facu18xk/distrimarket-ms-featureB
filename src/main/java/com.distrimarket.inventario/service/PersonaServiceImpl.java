package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.enums.TipoPersona;
import com.distrimarket.inventario.config.CacheConfig;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.mapper.PersonaMapper;
import com.distrimarket.inventario.repository.PersonaRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.PersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, PersonaRequestDTO, PersonaResponseDTO> implements PersonaService {

    private final PersonaRepository personaRepository;
    private final CacheManager cacheManager;

    public PersonaServiceImpl(PersonaRepository personaRepository, PersonaMapper personaMapper, CacheManager cacheManager) {
        super(personaRepository, personaMapper);
        this.personaRepository = personaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PERSONA, key = "#id")
    public PersonaResponseDTO findById(Long id) {
        log.debug("Cache miss para persona ID {}. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PERSONA, key = "'ci:' + #ci")
    public PersonaResponseDTO findByCi(String ci) {
        log.debug("Cache miss para persona con CI {}. Consultando base de datos...", ci);
        Persona persona = personaRepository.findByCi(ci)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona con el CI: " + ci));
        return mapper.toDTO(persona);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PERSONA, key = "'ruc:' + #ruc")
    public PersonaResponseDTO findByRuc(String ruc) {
        log.debug("Cache miss para persona con RUC {}. Consultando base de datos...", ruc);
        Persona persona = personaRepository.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona con el RUC: " + ruc));
        return mapper.toDTO(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonaResponseDTO> findAll(Pageable pageable) {
        Page<PersonaResponseDTO> page = super.findAll(pageable);
        Cache cache = cacheManager.getCache(CacheConfig.PERSONA);
        if (cache != null) {
            page.forEach(p -> {
                if (p.getId() != null) {
                    cache.put(p.getId(), p);
                    if (p.getCi() != null && !p.getCi().isBlank()) {
                        cache.put("ci:" + p.getCi(), p);
                    }
                    if (p.getRuc() != null && !p.getRuc().isBlank()) {
                        cache.put("ruc:" + p.getRuc(), p);
                    }
                }
            });
            log.debug("Se poblaron {} personas individualmente en caché desde la página {}",
                    page.getNumberOfElements(), pageable.getPageNumber());
        }
        return page;
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.PERSONA, key = "#result.id", condition = "#result != null && #result.id != null")
    public PersonaResponseDTO create(PersonaRequestDTO requestDTO) {
        validateCreate(requestDTO);
        PersonaResponseDTO creada = super.create(requestDTO);

        Cache cache = cacheManager.getCache(CacheConfig.PERSONA);
        if (cache != null) {
            if (creada.getCi() != null && !creada.getCi().isBlank()) {
                cache.put("ci:" + creada.getCi(), creada);
            }
            if (creada.getRuc() != null && !creada.getRuc().isBlank()) {
                cache.put("ruc:" + creada.getRuc(), creada);
            }
        }

        return creada;
    }

    @Override
    @Transactional
    public PersonaResponseDTO update(Long id, PersonaRequestDTO requestDTO) {
        Persona actual = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra la persona con ID: " + id));

        validateUpdate(id, requestDTO);
        deletePersonaKeys(id, actual.getCi(), actual.getRuc());

        return super.update(id, requestDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra la persona con ID: " + id));

        deletePersonaKeys(id, persona.getCi(), persona.getRuc());
        super.deleteById(id);
    }

    private void deletePersonaKeys(Long id, String ci, String ruc) {
        Cache cache = cacheManager.getCache(CacheConfig.PERSONA);
        if (cache != null) {
            cache.evict(id);
            if (ci != null && !ci.isBlank()) {
                cache.evict("ci:" + ci);
            }
            if (ruc != null && !ruc.isBlank()) {
                cache.evict("ruc:" + ruc);
            }
            log.debug("Caché invalidado para persona ID {}, CI {} y RUC {}", id, ci, ruc);
        }
    }

    private void validateCreate(PersonaRequestDTO dto) {
        log.debug("Validando reglas documentales para nueva persona [{}] - CI: '{}', RUC: '{}'",
                dto.getTipoPersona(), dto.getCi(), dto.getRuc());
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
        log.debug("Validando unicidad de documentos para actualización de persona ID: {}", id);
        Persona actual = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra la persona con ID: " + id));

        if (dto.getCi() != null && !dto.getCi().equals(actual.getCi())) {
            if (personaRepository.existsByCi(dto.getCi())) {
                throw new DuplicateResourceException("El nuevo CI ya pertenece a otra persona: " + dto.getCi());
            }
        }

        if (dto.getRuc() != null && !dto.getRuc().equals(actual.getRuc())) {
            if (personaRepository.existsByRuc(dto.getRuc())) {
                throw new DuplicateResourceException("El nuevo RUC ya pertenece a otra persona: " + dto.getRuc());
            }
        }
    }
}