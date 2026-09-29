package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockDetalleResponseDTO;
import com.distrimarket.commons.entity.AjusteStockDetalle;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AjusteStockDetalleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "ajusteStock", ignore = true)
    @Mapping(target = "producto.id", source = "idProducto")
    AjusteStockDetalle toEntity(AjusteStockDetalleRequestDTO dto);

    @Mapping(target = "idAjuste", source = "ajusteStock.id")
    @Mapping(target = "idProducto", source = "producto.id")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    AjusteStockDetalleResponseDTO toDTO(AjusteStockDetalle entity);

    List<AjusteStockDetalleResponseDTO> toDTOList(List<AjusteStockDetalle> entities);
}