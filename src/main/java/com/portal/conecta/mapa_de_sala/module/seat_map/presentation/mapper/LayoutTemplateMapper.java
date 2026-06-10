package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateLayoutTemplateCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateLayoutTemplateRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateLayoutTemplateRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutPositionItemResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.LayoutTemplateWithPositionsResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LayoutTemplateMapper {

    LayoutTemplateResponse toResponse(LayoutTemplate entity);

    List<LayoutTemplateResponse> toResponseList(List<LayoutTemplate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "positions", ignore = true)
    @Mapping(target = "roomLayouts", ignore = true)
    @Mapping(target = "roomMaps", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    LayoutTemplate toEntity(CreateLayoutTemplateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "positions", ignore = true)
    @Mapping(target = "roomLayouts", ignore = true)
    @Mapping(target = "roomMaps", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void applyUpdate(UpdateLayoutTemplateRequest request, @MappingTarget LayoutTemplate entity);

    default UpdateLayoutTemplateCommand toCommand(UUID id, UpdateLayoutTemplateRequest request) {
        return new UpdateLayoutTemplateCommand(id, request);
    }

    default LayoutTemplateWithPositionsResponse toWithPositionsResponse(LayoutTemplate template, List<LayoutPosition> positions) {
        List<LayoutPositionItemResponse> items = positions.stream()
                .map(p -> new LayoutPositionItemResponse(p.getPositionX(), p.getPositionY(), p.getType()))
                .toList();
        return new LayoutTemplateWithPositionsResponse(
                template.getId(),
                template.getDimensionX(),
                template.getDimensionY(),
                items
        );
    }
}