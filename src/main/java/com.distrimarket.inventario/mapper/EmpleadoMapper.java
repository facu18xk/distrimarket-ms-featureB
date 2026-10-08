package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PersonaMapper.class}, builder = @Builder(disableBuilder = true))
public interface EmpleadoMapper extends BaseMapper<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "activo", ignore = true)
    Empleado toEntity(EmpleadoRequestDTO dto);

    @Override
    @Mapping(target = "persona", source = "persona")
    EmpleadoResponseDTO toDTO(Empleado entity);

    @Override
    List<EmpleadoResponseDTO> toDTOList(List<Empleado> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromDto(EmpleadoRequestDTO dto, @MappingTarget Empleado entity);
}