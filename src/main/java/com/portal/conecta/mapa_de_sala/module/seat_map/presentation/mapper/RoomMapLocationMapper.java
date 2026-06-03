package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.MoveStudentCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapLocationCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapLocationRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.MoveStudentRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapLocationRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapLocationResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RoomMapLocationMapper {

    @Mapping(source = "roomMap.id", target = "roomMapId")
    @Mapping(source = "layoutPosition.id", target = "layoutPositionId")
    RoomMapLocationResponse toResponse(RoomMapLocation entity);

    List<RoomMapLocationResponse> toResponseList(List<RoomMapLocation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomMap", ignore = true)
    @Mapping(target = "layoutPosition", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RoomMapLocation toEntity(CreateRoomMapLocationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomMap", ignore = true)
    @Mapping(target = "layoutPosition", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void applyUpdate(UpdateRoomMapLocationRequest request, @MappingTarget RoomMapLocation entity);

    default UpdateRoomMapLocationCommand toCommand(UUID id, UpdateRoomMapLocationRequest request) {
        return new UpdateRoomMapLocationCommand(id, request);
    }

    default MoveStudentCommand toMoveCommand(UUID roomMapId, MoveStudentRequest request) {
        return new MoveStudentCommand(roomMapId, request);
    }
}