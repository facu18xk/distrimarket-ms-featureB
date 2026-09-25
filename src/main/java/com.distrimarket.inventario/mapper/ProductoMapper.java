package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.Producto;
import com.distrimarket.commons.dto.ProductoRequestDTO;
import com.distrimarket.commons.dto.ProductoResponseDTO;
import com.distrimarket.commons.dto.ProductoRequestDTO.PorcentajeIvaEnum;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductoMapper extends BaseMapper<Producto, ProductoRequestDTO, ProductoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "categoria.id", source = "idCategoria")
    @Mapping(target = "marca.id", source = "idMarca")
    Producto toEntity(ProductoRequestDTO dto);

    @Override
    @Mapping(target = "idCategoria", source = "categoria.id")
    @Mapping(target = "idMarca", source = "marca.id")
    @Mapping(target = "stockActual", ignore = true)
    ProductoResponseDTO toDTO(Producto entity);

    @Override
    List<ProductoResponseDTO> toDTOList(List<Producto> entityList);

    default BigDecimal mapPorcentajeIva(PorcentajeIvaEnum value) {
        if (value == null) {
            return null;
        }
        /*return switch (value) {
            case NUMBER_0 -> new BigDecimal("0.00");
            case NUMBER_5 -> new BigDecimal("5.00");
            case NUMBER_10 -> new BigDecimal("10.00");
            default -> throw new IllegalArgumentException("Valor de IVA inesperado de la API: " + value);
        };*/
        return value.getValue();
    }
}