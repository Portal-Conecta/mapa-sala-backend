package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateLayoutPositionCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateLayoutPositionRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateLayoutPositionRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LayoutPositionMapper {

    @Mapping(source = "layoutTemplate.id", target = "layoutTemplateId")
    LayoutPositionResponse toResponse(LayoutPosition entity);

    List<LayoutPositionResponse> toResponseList(List<LayoutPosition> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layoutTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    LayoutPosition toEntity(CreateLayoutPositionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layoutTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void applyUpdate(UpdateLayoutPositionRequest request, @MappingTarget LayoutPosition entity);

    default UpdateLayoutPositionCommand toCommand(UUID id, UpdateLayoutPositionRequest request) {
        return new UpdateLayoutPositionCommand(id, request);
    }
}