package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.inventario.config.CacheConfig;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.CategoriaMapper;
import com.distrimarket.inventario.repository.CategoriaRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.CategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoriaServiceImpl extends BaseServiceImpl<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> implements CategoriaService {

    /** Los listados se arman desde la caché de este dominio, objeto por objeto. */
    @Override
    protected String dominioCache() {
        return CacheConfig.CATEGORIA;
    }

    private final CategoriaRepository categoriaRepository;
    private final CacheManager cacheManager;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
                                CategoriaMapper categoriaMapper,
                                CacheManager cacheManager) {
        super(categoriaRepository, categoriaMapper);
        this.categoriaRepository = categoriaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CATEGORIA, key = "#id")
    public CategoriaResponseDTO findById(Long id) {
        log.debug("Cache miss para categoría ID {}. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> findAll(Pageable pageable) {
        Page<CategoriaResponseDTO> paginaCategorias = super.findAll(pageable);

        Cache cache = cacheManager.getCache(CacheConfig.CATEGORIA);
        if (cache != null) {
            paginaCategorias.forEach(categoria -> {
                if (categoria.getId() != null) {
                    cache.put(categoria.getId(), categoria);
                }
            });
            log.debug("Se almacenaron {} categorías de la página {} individualmente en Redis",
                    paginaCategorias.getNumberOfElements(), pageable.getPageNumber());
        }

        return paginaCategorias;
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.CATEGORIA, key = "#result.id", condition = "#result != null && #result.id != null")
    public CategoriaResponseDTO create(CategoriaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de categoría: '{}'", createDTO.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    // OPCIÓN A: Invalidación con @CacheEvict (Recomendada)
    // Borra la clave de Redis. La próxima lectura forzará consulta limpia a la BD.
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORIA, key = "#id")
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para categoría ID {}: '{}'", id, createDTO.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre: " + createDTO.getNombre());
        }
        log.debug("Actualizando categoría ID {} e invalidando su entrada en Redis", id);
        return super.update(id, createDTO);
    }

    /*
    // OPCIÓN B: Sobrescribir con @CachePut (Si el profesor te pide demostrar la segunda alternativa)
    // Actualiza la BD y reemplaza directamente el valor en Redis con el DTO resultante.
    @Override
    @Transactional
    @CachePut(value = "categorias", key = "'api_categoria_' + #result.id", condition = "#result != null && #result.id != null")
    public CategoriaResponseDTO update(Long id, CategoriaRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para categoría ID {}: '{}'", id, createDTO.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre: " + createDTO.getNombre());
        }
        log.debug("Actualizando categoría ID {} y reescribiendo el valor en Redis", id);
        return super.update(id, createDTO);
    }
    */

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORIA, key = "#id")
    public void deleteById(Long id) {
        log.debug("Eliminando categoría ID {} y desalojando clave de Redis", id);
        super.deleteById(id);
    }
}