package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.TransferenciaStockDetalleRequestDTO;
import com.distrimarket.commons.dto.TransferenciaStockDetalleResponseDTO;
import com.distrimarket.commons.entity.TransferenciaStockDetalle;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TransferenciaStockDetalleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "transferenciaStock", ignore = true)
    @Mapping(target = "producto.id", source = "idProducto")
    TransferenciaStockDetalle toEntity(TransferenciaStockDetalleRequestDTO dto);

    @Mapping(target = "idTransferencia", source = "transferenciaStock.id")
    @Mapping(target = "idProducto", source = "producto.id")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    TransferenciaStockDetalleResponseDTO toDTO(TransferenciaStockDetalle entity);

    List<TransferenciaStockDetalleResponseDTO> toDTOList(List<TransferenciaStockDetalle> entities);
}