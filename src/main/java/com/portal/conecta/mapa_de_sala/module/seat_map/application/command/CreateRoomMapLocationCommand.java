package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapLocationRequest;
import java.util.UUID;

public record CreateRoomMapLocationCommand(
        CreateRoomMapLocationRequest data,
        UUID actorUserId
) {
}