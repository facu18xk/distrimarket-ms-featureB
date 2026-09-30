package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.TransferenciaStockRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockResponseDTO;
import com.distrimarket.commons.entity.TransferenciaStock;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TransferenciaStockDetalleMapper.class}, builder = @Builder(disableBuilder = true))
public interface TransferenciaStockMapper extends BaseMapper<TransferenciaStock, TransferenciaStockRequestDTO, TransferenciaStockResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "depositoOrigen.id", source = "idDepositoOrigen")
    @Mapping(target = "depositoDestino.id", source = "idDepositoDestino")
    @Mapping(target = "empleado.id", source = "idEmpleado")
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    TransferenciaStock toEntity(TransferenciaStockRequestDTO dto);

    @Override
    @Mapping(target = "idDepositoOrigen", source = "depositoOrigen.id")
    @Mapping(target = "nombreDepositoOrigen", source = "depositoOrigen.nombre")
    @Mapping(target = "idDepositoDestino", source = "depositoDestino.id")
    @Mapping(target = "nombreDepositoDestino", source = "depositoDestino.nombre")
    @Mapping(target = "idEmpleado", source = "empleado.id")
    @Mapping(target = "nombreEmpleado", source = "empleado.persona.nombreCompleto")
    @Mapping(target = "detalles", source = "detalles")
    TransferenciaStockResponseDTO toDTO(TransferenciaStock entity);

    @Override
    List<TransferenciaStockResponseDTO> toDTOList(List<TransferenciaStock> entityList);

    @Override
    default void updateEntityFromDto(TransferenciaStockRequestDTO dto, @MappingTarget TransferenciaStock entity) {
        throw new UnsupportedOperationException("Las transferencias de stock son inmutables.");
    }
}