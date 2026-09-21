package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseService<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO> {
    Page<RESPONSE_DTO> findAll(Pageable pageable);
    RESPONSE_DTO findById(Long id);
    RESPONSE_DTO create(CREATE_DTO createDTO);
    RESPONSE_DTO update(Long id, CREATE_DTO createDTO);
    void deleteById(Long id);
}