package com.distrimarket.inventario.service;

import com.distrimarket.commons.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BaseService<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO> {
    List<RESPONSE_DTO> findAll();
    RESPONSE_DTO findById(Long id);
    RESPONSE_DTO create(CREATE_DTO createDTO);
    RESPONSE_DTO update(Long id, CREATE_DTO createDTO);
    void deleteById(Long id);
}