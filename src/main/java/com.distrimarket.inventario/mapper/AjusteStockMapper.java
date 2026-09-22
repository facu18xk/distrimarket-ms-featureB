package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.AjusteStockDetalleRequestDTO;
import com.distrimarket.commons.dto.AjusteStockDetalleResponseDTO;
import com.distrimarket.commons.dto.AjusteStockRequestDTO;
import com.distrimarket.commons.dto.AjusteStockResponseDTO;
import com.distrimarket.commons.entity.AjusteStock;
import com.distrimarket.commons.entity.AjusteStockDetalle;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AjusteStockMapper extends BaseMapper<AjusteStock, AjusteStockRequestDTO, AjusteStockResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito", ignore = true)   // Se asigna manualmente en el Service
    @Mapping(target = "empleado", ignore = true)   // Se asigna manualmente en el Service
    @Mapping(target = "detalles", ignore = true)   // Se procesan manualmente en el Service
    AjusteStock toEntity(AjusteStockRequestDTO dto);

    @Override
    @Mapping(target = "idAjuste", source = "id")
    @Mapping(source = "deposito.id", target = "idDeposito")
    @Mapping(source = "deposito.nombre", target = "nombreDeposito")
    @Mapping(source = "empleado.id", target = "idEmpleado")
    //@Mapping(target = "nombreEmpleado", expression = "java(entity.getEmpleado() != null ? entity.getEmpleado().getNombre() + \" \" + entity.getEmpleado().getApellido() : null)")
    @Mapping(target = "nombreEmpleado", expression = "java(entity.getEmpleado() != null && entity.getEmpleado().getPersona() != null " + "? entity.getEmpleado().getPersona().getNombreCompleto() : null)")
    AjusteStockResponseDTO toDTO(AjusteStock entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "deposito", ignore = true)
    @Mapping(target = "empleado", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void updateEntityFromDTO(AjusteStockRequestDTO dto, @MappingTarget AjusteStock entity);

    @Mapping(target = "idAjuste", source = "id")
    @Mapping(source = "producto.id", target = "idProducto")
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    AjusteStockDetalleResponseDTO toDetalleDTO(AjusteStockDetalle detalle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "ajusteStock", ignore = true) // Se asigna mediante addDetalle(...) en la entidad
    @Mapping(target = "producto", ignore = true)   // Se asigna en el Service
    AjusteStockDetalle toDetalleEntity(AjusteStockDetalleRequestDTO dto);
}