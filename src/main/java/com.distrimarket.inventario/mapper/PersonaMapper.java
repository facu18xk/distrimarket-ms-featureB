package com.distrimarket.inventario.mapper;

import com.distrimarket.commons.dto.PersonaRequestDTO;
import com.distrimarket.commons.dto.PersonaResponseDTO;
import com.distrimarket.commons.entity.Persona;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PersonaMapper extends BaseMapper<Persona, PersonaRequestDTO, PersonaResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    Persona toEntity(PersonaRequestDTO dto);

    @Override
    @Mapping(target = "idPersona", source = "id")
    PersonaResponseDTO toDTO(Persona entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDTO(PersonaRequestDTO dto, @MappingTarget Persona entity);
}