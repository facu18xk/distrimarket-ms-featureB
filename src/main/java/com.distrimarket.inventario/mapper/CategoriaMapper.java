package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.Categoria;
import com.distrimarket.commons.dto.CategoriaRequestDTO;
import com.distrimarket.commons.dto.CategoriaResponseDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CategoriaMapper extends BaseMapper<Categoria, CategoriaRequestDTO, CategoriaResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);

    @Override
    //@Mapping(target = "id", source = "id")
    CategoriaResponseDTO toDTO(Categoria entity);

    @Override
    List<CategoriaResponseDTO> toDTOList(List<Categoria> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO createDto, @MappingTarget Categoria entity);
}