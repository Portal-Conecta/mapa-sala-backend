package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import java.util.UUID;

public record RoomLayoutResponse(
        UUID id,
        UUID roomId,
        UUID layoutTemplateId
) {
}