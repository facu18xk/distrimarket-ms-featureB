package com.distrimarket.inventario.service;

import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.inventario.config.CacheConfig;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.DepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.DepositoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DepositoServiceImpl extends BaseServiceImpl<Deposito, DepositoRequestDTO, DepositoResponseDTO> implements DepositoService {

    /** Los listados se arman desde la caché de este dominio, objeto por objeto. */
    @Override
    protected String dominioCache() {
        return CacheConfig.DEPOSITO;
    }

    private final DepositoRepository depositoRepository;
    private final CacheManager cacheManager;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoMapper depositoMapper, CacheManager cacheManager) {
        super(depositoRepository, depositoMapper);
        this.depositoRepository = depositoRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.DEPOSITO, key = "#id")
    public DepositoResponseDTO findById(Long id) {
        log.debug("Cache miss para depósito ID {}. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepositoResponseDTO> findAll(Pageable pageable) {
        Page<DepositoResponseDTO> paginaDepositos = super.findAll(pageable);

        Cache cache = cacheManager.getCache(CacheConfig.DEPOSITO);
        if (cache != null) {
            paginaDepositos.forEach(deposito -> {
                if (deposito.getId() != null) {
                    cache.put(deposito.getId(), deposito);
                }
            });
            log.debug("Se almacenaron {} depósitos de la página {} individualmente en Redis",
                    paginaDepositos.getNumberOfElements(), pageable.getPageNumber());
        }

        return paginaDepositos;
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.DEPOSITO, key = "#result.id", condition = "#result != null && #result.id != null")
    public DepositoResponseDTO create(DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de depósito: '{}'", createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe un depósito con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DEPOSITO, key = "#id")
    public DepositoResponseDTO update(Long id, DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para depósito ID {}: '{}'", id, createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otro depósito con el nombre: " + createDTO.getNombre());
        }
        log.debug("Actualizando depósito ID {} e invalidando clave de Redis", id);
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DEPOSITO, key = "#id")
    public void deleteById(Long id) {
        log.debug("Eliminando depósito ID {} y desalojando clave de Redis", id);
        super.deleteById(id);
    }
}