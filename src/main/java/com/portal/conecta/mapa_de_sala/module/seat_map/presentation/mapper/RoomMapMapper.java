package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapDetailResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapLocationResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapWithLocationsResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RoomMapMapper {

    @Mapping(source = "layoutTemplate.id", target = "layoutTemplateId")
    RoomMapResponse toResponse(RoomMap entity);

    @Mapping(source = "layoutTemplate.id", target = "layoutTemplateId")
    RoomMapDetailResponse toDetailResponse(RoomMap entity);

    List<RoomMapResponse> toResponseList(List<RoomMap> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layoutTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RoomMap toEntity(CreateRoomMapRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "layoutTemplate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void applyUpdate(UpdateRoomMapRequest request, @MappingTarget RoomMap entity);

    default UpdateRoomMapCommand toCommand(UUID id, UpdateRoomMapRequest request) {
        return new UpdateRoomMapCommand(id, request);
    }

    default RoomMapWithLocationsResponse toWithLocationsResponse(RoomMapResponse map, List<RoomMapLocationResponse> locations) {
        return new RoomMapWithLocationsResponse(map, locations);
    }
}