package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.BaseEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface BaseMapper<E extends BaseEntity, CREATE_DTO, RESPONSE_DTO> {
    E toEntity(CREATE_DTO dto);
    RESPONSE_DTO toDTO(E entity);
    List<RESPONSE_DTO> toDTOList(List<E> entityList);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDto(CREATE_DTO dto, @MappingTarget E entity);
}