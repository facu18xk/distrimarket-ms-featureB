package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.entity.Persona;
import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PersonaMapper extends BaseMapper<Persona, PersonaRequestDTO, PersonaResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Persona toEntity(PersonaRequestDTO dto);

    @Override
    PersonaResponseDTO toDTO(Persona entity);

    @Override
    List<PersonaResponseDTO> toDTOList(List<Persona> entityList);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDto(PersonaRequestDTO dto, @MappingTarget Persona entity);
}