package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.BaseEntity;
import com.distrimarket.inventario.mapper.BaseMapper;
import com.distrimarket.inventario.repository.BaseRepository;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseServiceImpl<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO>
        implements BaseService<E, CREATE_DTO, RESPONSE_DTO> {

    protected final BaseRepository<E> repository;
    protected final BaseMapper<E, CREATE_DTO, RESPONSE_DTO> mapper;

    public BaseServiceImpl(BaseRepository<E> repository, BaseMapper<E, CREATE_DTO, RESPONSE_DTO> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /*@Override
    @Transactional(readOnly = true)
    public List<RESPONSE_DTO> findAll() {
        List<E> entityList = repository.findAll();
        return mapper.toDTOList(entityList);
    }*/

    @Override
    @Transactional(readOnly = true)
    public Page<RESPONSE_DTO> findAll(Pageable pageable) {
        Page<E> entityPage = repository.findAll(pageable);
        return entityPage.map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RESPONSE_DTO findById(Long id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con ID: " + id));
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public RESPONSE_DTO create(CREATE_DTO createDTO) {
        E entity = mapper.toEntity(createDTO);
        E savedEntity = repository.save(entity);
        return mapper.toDTO(savedEntity);
    }

    @Override
    @Transactional
    public RESPONSE_DTO update(Long id, CREATE_DTO createDTO) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No se encuentra el registro para actualizar con ID: " + id);
        }
        E entity = mapper.toEntity(createDTO);
        entity.setId(id);
        E updatedEntity = repository.save(entity);
        return mapper.toDTO(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("No se encuentra el registro con ID: " + id);
        }
        repository.deleteById(id);
    }
}