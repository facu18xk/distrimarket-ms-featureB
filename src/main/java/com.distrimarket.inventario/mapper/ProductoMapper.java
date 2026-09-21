package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.ProductoCreateDTO;
import com.distrimarket.commons.dto.ProductoDetailResponseDTO;
import com.distrimarket.commons.entity.Producto;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductoMapper extends BaseMapper<Producto, ProductoCreateDTO, ProductoDetailResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true) // Se ignora el ID herencia de BaseEntity al crear
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "categoria.id", source = "idCategoria")
    @Mapping(target = "marca.id", source = "idMarca")
    @Mapping(target = "porcentajeIva", source = "porcentajeIva", qualifiedByName = "enumToBigDecimal")
    Producto toEntity(ProductoCreateDTO dto);

    @Override
    @Mapping(target = "idProducto", source = "id") // Mapea el id heredado de BaseEntity al idProducto del DTO de respuesta
    @Mapping(target = "stockActual", ignore = true)
    ProductoDetailResponseDTO toDTO(Producto entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "categoria.id", source = "idCategoria")
    @Mapping(target = "marca.id", source = "idMarca")
    @Mapping(target = "porcentajeIva", source = "porcentajeIva", qualifiedByName = "enumToBigDecimal")
    void updateEntityFromDTO(ProductoCreateDTO createDto, @MappingTarget Producto entity);

    @Named("enumToBigDecimal")
    default BigDecimal enumToBigDecimal(ProductoCreateDTO.PorcentajeIvaEnum porcentajeIvaEnum) {
        if (porcentajeIvaEnum == null) {
            return BigDecimal.ZERO;
        }

        String name = porcentajeIvaEnum.name();

        if (name.contains("10")) {
            return new BigDecimal("10.0");
        } else if (name.contains("5")) {
            return new BigDecimal("5.0");
        } else {
            return BigDecimal.ZERO;
        }
    }
}