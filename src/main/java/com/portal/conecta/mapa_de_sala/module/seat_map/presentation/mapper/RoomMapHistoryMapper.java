package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapHistoryCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapHistoryRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RoomMapHistoryMapper {

    @Mapping(source = "roomMap.id", target = "roomMapId")
    RoomMapHistoryResponse toResponse(RoomMapHistory entity);

    List<RoomMapHistoryResponse> toResponseList(List<RoomMapHistory> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomMap", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RoomMapHistory toEntity(CreateRoomMapHistoryRequest request);

    default CreateRoomMapHistoryCommand toCommand(CreateRoomMapHistoryRequest request, UUID userId) {
        return new CreateRoomMapHistoryCommand(request, userId);
    }
}