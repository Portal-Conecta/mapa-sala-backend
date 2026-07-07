package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapAllocationsRequest;

import java.util.UUID;

public record UpdateRoomMapAllocationsCommand(
        UUID roomMapId,
        UpdateRoomMapAllocationsRequest data
) {
}