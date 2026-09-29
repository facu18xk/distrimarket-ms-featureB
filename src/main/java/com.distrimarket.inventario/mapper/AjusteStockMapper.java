package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AjusteStockDetalleMapper.class}, builder = @Builder(disableBuilder = true))
public interface AjusteStockMapper extends BaseMapper<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito.id", source = "idDeposito")
    @Mapping(target = "empleado.id", source = "idEmpleado")
    @Mapping(target = "detalles", ignore = true) // Los detalles se vinculan con método bidireccional addDetalle
    AjusteStock toEntity(AjusteStockRequestDTO dto);

    @Override
    @Mapping(target = "idDeposito", source = "deposito.id")
    @Mapping(target = "nombreDeposito", source = "deposito.nombre")
    @Mapping(target = "idEmpleado", source = "empleado.id")
    @Mapping(target = "nombreEmpleado", source = "empleado.persona.nombreCompleto")
    @Mapping(target = "detalles", source = "detalles")
    AjusteStockResponseDTO toDTO(AjusteStock entity);

    @Override
    List<AjusteStockResponseDTO> toDTOList(List<AjusteStock> entityList);

    @Override
    default void updateEntityFromDto(AjusteStockRequestDTO dto, @MappingTarget AjusteStock entity) {
        throw new UnsupportedOperationException("Los ajustes de stock son transacciones inmutables y no admiten actualización.");
    }
}