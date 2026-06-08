package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomLayoutRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomLayoutResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomLayoutMapper {

    @Mapping(source = "layoutTemplate.id", target = "layoutTemplateId")
    RoomLayoutResponse toResponse(RoomLayout entity);

    List<RoomLayoutResponse> toResponseList(List<RoomLayout> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layoutTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RoomLayout toEntity(CreateRoomLayoutRequest request);
}