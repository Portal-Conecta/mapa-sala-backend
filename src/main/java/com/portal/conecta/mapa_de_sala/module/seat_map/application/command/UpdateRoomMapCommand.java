package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapRequest;
import java.util.UUID;

public record UpdateRoomMapCommand(
        UUID id,
        UpdateRoomMapRequest data
) {
}