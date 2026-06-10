package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record RoomMapDetailResponse(
        UUID id,
        UUID classId,
        UUID roomId,
        UUID layoutTemplateId,
        List<RoomMapLocationResponse> locations
) {
}