package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.StockDeposito;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface StockDepositoMapper extends BaseMapper<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito.id", source = "idDeposito")
    @Mapping(target = "producto.id", source = "idProducto")
    StockDeposito toEntity(StockDepositoRequestDTO dto);

    @Override
    @Mapping(target = "idDeposito", source = "deposito.id")
    @Mapping(target = "nombreDeposito", source = "deposito.nombre")
    @Mapping(target = "idProducto", source = "producto.id")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    StockDepositoResponseDTO toDTO(StockDeposito entity);

    @Override
    List<StockDepositoResponseDTO> toDTOList(List<StockDeposito> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito.id", source = "idDeposito")
    @Mapping(target = "producto.id", source = "idProducto")
    void updateEntityFromDto(StockDepositoRequestDTO dto, @MappingTarget StockDeposito entity);
}