package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.inventario.exception.DuplicateResourceException;
import com.distrimarket.inventario.mapper.DepositoMapper;
import com.distrimarket.inventario.repository.DepositoRepository;
import com.distrimarket.inventario.service.BaseServiceImpl;
import com.distrimarket.inventario.service.DepositoService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DepositoServiceImpl extends BaseServiceImpl<Deposito, DepositoRequestDTO, DepositoResponseDTO> implements DepositoService {

    private final DepositoRepository depositoRepository;
    private final CacheManager cacheManager;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoMapper depositoMapper, CacheManager cacheManager) {
        super(depositoRepository, depositoMapper);
        this.depositoRepository = depositoRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "depositos", key = "'api_deposito_' + #id")
    public DepositoResponseDTO findById(Long id) {
        log.debug("Cache miss para 'api_deposito_{}'. Consultando base de datos...", id);
        return super.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepositoResponseDTO> findAll(Pageable pageable) {
        Page<DepositoResponseDTO> paginaDepositos = super.findAll(pageable);

        Cache cache = cacheManager.getCache("depositos");
        if (cache != null) {
            paginaDepositos.forEach(deposito -> {
                if (deposito.getId() != null) {
                    String customKey = "api_deposito_" + deposito.getId();
                    cache.put(customKey, deposito);
                }
            });
            log.debug("Se almacenaron {} depósitos individualmente en caché bajo 'api_deposito_*' desde la página {}",
                    paginaDepositos.getNumberOfElements(), pageable.getPageNumber());
        }

        return paginaDepositos;
    }

    @Override
    @Transactional
    @CachePut(value = "depositos", key = "'api_deposito_' + #result.id", condition = "#result != null && #result.id != null")
    public DepositoResponseDTO create(DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre de depósito: '{}'", createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCase(createDTO.getNombre())) {
            throw new DuplicateResourceException("Ya existe un depósito con el nombre: " + createDTO.getNombre());
        }
        return super.create(createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "depositos", key = "'api_deposito_' + #id")
    public DepositoResponseDTO update(Long id, DepositoRequestDTO createDTO) {
        log.debug("Comprobando disponibilidad de nombre para depósito ID {}: '{}'", id, createDTO.getNombre());
        if (depositoRepository.existsByNombreIgnoreCaseAndIdNot(createDTO.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe otro depósito con el nombre: " + createDTO.getNombre());
        }
        log.debug("Actualizando depósito ID {} y desalojando clave 'api_deposito_{}' de Redis", id, id);
        return super.update(id, createDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "depositos", key = "'api_deposito_' + #id")
    public void deleteById(Long id) {
        log.debug("Eliminando depósito ID {} y desalojando clave 'api_deposito_{}' de Redis", id, id);
        super.deleteById(id);
    }
}