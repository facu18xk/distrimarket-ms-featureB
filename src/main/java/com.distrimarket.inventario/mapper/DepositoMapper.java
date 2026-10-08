package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.Deposito;
import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DepositoMapper extends BaseMapper<Deposito, DepositoRequestDTO, DepositoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Deposito toEntity(DepositoRequestDTO dto);

    @Override
    DepositoResponseDTO toDTO(Deposito entity);

    @Override
    List<DepositoResponseDTO> toDTOList(List<Deposito> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromDto(DepositoRequestDTO dto, @MappingTarget Deposito entity);
}