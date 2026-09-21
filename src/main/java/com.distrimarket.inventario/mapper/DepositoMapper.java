package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.DepositoRequestDTO;
import com.distrimarket.commons.dto.DepositoResponseDTO;
import com.distrimarket.commons.entity.Deposito;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DepositoMapper extends BaseMapper<Deposito, DepositoRequestDTO, DepositoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Deposito toEntity(DepositoRequestDTO dto);

    @Override
    @Mapping(target = "idDeposito", source = "id")
    DepositoResponseDTO toDTO(Deposito entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDTO(DepositoRequestDTO createDto, @MappingTarget Deposito entity);
}