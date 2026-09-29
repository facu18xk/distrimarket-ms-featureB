package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmpleadoMapper extends BaseMapper<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona.id", source = "idPersona")
    Empleado toEntity(EmpleadoRequestDTO dto);

    @Override
    @Mapping(target = "idPersona", source = "persona.id")
    @Mapping(target = "nombreCompletoPersona", source = "persona.nombreCompleto")
    @Mapping(target = "ciPersona", source = "persona.ci")
    EmpleadoResponseDTO toDTO(Empleado entity);

    @Override
    List<EmpleadoResponseDTO> toDTOList(List<Empleado> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona.id", source = "idPersona")
    void updateEntityFromDto(EmpleadoRequestDTO dto, @MappingTarget Empleado entity);
}