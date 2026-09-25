package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.Marca;
import com.distrimarket.commons.dto.MarcaRequestDTO;
import com.distrimarket.commons.dto.MarcaResponseDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MarcaMapper extends BaseMapper<Marca, MarcaRequestDTO, MarcaResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Marca toEntity(MarcaRequestDTO dto);

    @Override
    MarcaResponseDTO toDTO(Marca entity);

    @Override
    List<MarcaResponseDTO> toDTOList(List<Marca> entityList);
}