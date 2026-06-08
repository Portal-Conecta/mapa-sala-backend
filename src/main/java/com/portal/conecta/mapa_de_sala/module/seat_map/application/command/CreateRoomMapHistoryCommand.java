package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapHistoryRequest;

import java.util.UUID;

public record CreateRoomMapHistoryCommand(
        CreateRoomMapHistoryRequest data,
        UUID userId
) {
}