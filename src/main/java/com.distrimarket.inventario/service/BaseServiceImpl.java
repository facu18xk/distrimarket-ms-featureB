package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.BaseEntity;
import com.distrimarket.commons.model.SoftDeletable;
import com.distrimarket.inventario.mapper.BaseMapper;
import com.distrimarket.inventario.repository.BaseRepository;
import com.distrimarket.inventario.exception.ResourceNotFoundException;
import com.distrimarket.inventario.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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
        log.debug("[{}] Consultando página {} con tamaño {}", getServiceName(), pageable.getPageNumber(), pageable.getPageSize());
        Page<E> entityPage = repository.findAll(pageable);
        return entityPage.map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RESPONSE_DTO findById(Long id) {
        log.debug("[{}] Buscando registro con ID: {}", getServiceName(), id);
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con ID: " + id));
        // Si es SoftDeletable y está inactivo, se considera no encontrado para consultas normales
        if (entity instanceof SoftDeletable softDeletable && Boolean.FALSE.equals(softDeletable.getActivo())) {
            throw new ResourceNotFoundException("Recurso inactivo o no disponible con ID: " + id);
        }
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public RESPONSE_DTO create(CREATE_DTO createDTO) {
        log.info("[{}] Creando nuevo registro...", getServiceName());
        E entity = mapper.toEntity(createDTO);
        // Si soporta borrado lógico, nace activo por defecto
        if (entity instanceof SoftDeletable softDeletable && softDeletable.getActivo() == null) {
            softDeletable.setActivo(true);
        }
        E savedEntity = repository.save(entity);
        log.info("[{}] Registro creado exitosamente con ID: {}", getServiceName(), savedEntity.getId());
        return mapper.toDTO(savedEntity);
    }

    @Override
    @Transactional
    public RESPONSE_DTO update(Long id, CREATE_DTO createDTO) {
        log.info("[{}] Actualizando registro con ID: {}", getServiceName(), id);
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el registro para actualizar con ID: " + id));
        // Si la entidad está inactiva, no se puede actualizar
        if (entity instanceof SoftDeletable softDeletable && Boolean.FALSE.equals(softDeletable.getActivo())) {
            throw new ResourceNotFoundException("No se puede actualizar un registro dado de baja (ID: " + id + ")");
        }
        mapper.updateEntityFromDto(createDTO, entity);
        E updatedEntity = repository.save(entity);
        log.info("[{}] Registro con ID: {} actualizado exitosamente", getServiceName(), id);
        return mapper.toDTO(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el registro con ID: " + id));

        if (entity instanceof SoftDeletable softDeletable) {
            log.info("[{}] Aplicando borrado lógico para registro con ID: {}", getServiceName(), id);
            softDeletable.setActivo(false);
            repository.save(entity);
            log.info("[{}] Registro con ID: {} marcado como inactivo (borrado lógico)", getServiceName(), id);
        } else {
            log.info("[{}] Aplicando borrado físico para registro con ID: {}", getServiceName(), id);
            repository.delete(entity);
            log.info("[{}] Registro con ID: {} eliminado físicamente de la base de datos", getServiceName(), id);
        }
    }

    @Transactional
    public void reactivarById(Long id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el registro con ID: " + id));

        if (entity instanceof SoftDeletable softDeletable) {
            softDeletable.setActivo(true);
            repository.save(entity);
            log.info("[{}] Registro ID: {} reactivado con éxito", getServiceName(), id);
        } else {
            throw new UnsupportedOperationException("Esta entidad no admite reactivación lógica.");
        }
    }

    // Helper para que el log indique qué servicio concreto está actuando
    protected String getServiceName() {
        return this.getClass().getSimpleName();
    }
}