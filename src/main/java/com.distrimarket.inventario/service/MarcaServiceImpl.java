package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.MarcaMapper;
import com.distrimarket.inventario.repository.MarcaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

@Slf4j
@Service
public class MarcaServiceImpl extends BaseServiceImpl<Marca, MarcaRequestDTO, MarcaResponseDTO> implements MarcaService {

    private final MarcaRepository marcaRepository;
    private final CacheManager cacheManager;

    public MarcaServiceImpl(MarcaRepository marcaRepository, MarcaMapper marcaMapper, CacheManager cacheManager) {
        super(marcaRepository, marcaMapper);
        this.marcaRepository = marcaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "marcas", key = "'api_marca_' + #id")
    public MarcaResponseDTO findById(Long id) {
        log.debug("Cache miss para 'api_marca_{}'. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MarcaResponseDTO> findAll(Pageable pageable) {
        Page<MarcaResponseDTO> paginaMarcas = super.findAll(pageable);

        Cache cache = cacheManager.getCache("marcas");
        if (cache != null) {
            paginaMarcas.forEach(marca -> {
                if (marca.getId() != null) {
                    String customKey = "api_marca_" + marca.getId();
                    cache.put(customKey, marca);
                }
            });
            log.debug("Se almacenaron {} marcas de la página {} individualmente en Redis con prefijo 'api_marca_'",
                    paginaMarcas.getNumberOfElements(), pageable.getPageNumber());
        }

        return paginaMarcas;
    }

    @Override
    @Transactional
    @CachePut(value = "marcas", key = "'api_marca_' + #result.id", condition = "#result != null && #result.id != null")
    public MarcaResponseDTO create(MarcaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de marca: '{}'", createDTO.getNombre());
        if (marcaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe una marca con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "marcas", key = "'api_marca_' + #id")
    public MarcaResponseDTO update(Long id, MarcaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para marca ID {}: '{}'", id, createDTO.getNombre());
        if (marcaRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otra marca con el nombre: " + createDTO.getNombre());
        }
        log.debug("Actualizando marca ID {} e invalidando su entrada en Redis", id);
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "marcas", key = "'api_marca_' + #id")
    public void deleteById(Long id) {
        log.debug("Eliminando marca ID {} y desalojando clave 'api_marca_{}'", id, id);
        super.deleteById(id);
    }
}