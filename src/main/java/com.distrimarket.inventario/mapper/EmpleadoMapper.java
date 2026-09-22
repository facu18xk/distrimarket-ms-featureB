package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.EmpleadoRequestDTO;
import com.distrimarket.commons.dto.EmpleadoResponseDTO;
import com.distrimarket.commons.entity.Empleado;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EmpleadoMapper extends BaseMapper<Empleado, EmpleadoRequestDTO, EmpleadoResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona", ignore = true) // Se vincula manualmente en el Service mediante el idPersona
    Empleado toEntity(EmpleadoRequestDTO dto);

    @Override
    @Mapping(target = "idEmpleado", source = "id")
    @Mapping(source = "persona.id", target = "idPersona")
    @Mapping(source = "persona.nombreCompleto", target = "nombreCompletoPersona")
    @Mapping(source = "persona.ci", target = "ciPersona")
    EmpleadoResponseDTO toDTO(Empleado entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "persona", ignore = true) // Se vincula manualmente en el Service
    void updateEntityFromDTO(EmpleadoRequestDTO dto, @MappingTarget Empleado entity);
}