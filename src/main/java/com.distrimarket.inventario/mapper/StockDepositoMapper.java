package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.StockDepositoRequestDTO;
import com.distrimarket.commons.dto.StockDepositoResponseDTO;
import com.distrimarket.commons.entity.StockDeposito;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface StockDepositoMapper extends BaseMapper<StockDeposito, StockDepositoRequestDTO, StockDepositoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito", ignore = true) // Se asigna manualmente en el servicio mediante el id
    @Mapping(target = "producto", ignore = true) // Se asigna manualmente en el servicio mediante el id
    StockDeposito toEntity(StockDepositoRequestDTO dto);

    @Override
    @Mapping(target = "idStock", source = "id")
    @Mapping(source = "deposito.id", target = "idDeposito")
    @Mapping(source = "deposito.nombre", target = "nombreDeposito")
    @Mapping(source = "producto.id", target = "idProducto")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    StockDepositoResponseDTO toDTO(StockDeposito entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito", ignore = true)
    @Mapping(target = "producto", ignore = true)
    void updateEntityFromDTO(StockDepositoRequestDTO createDto, @MappingTarget StockDeposito entity);
}