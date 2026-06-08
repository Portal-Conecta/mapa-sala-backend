package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import java.util.UUID;

public record UpdateRoomMapLocationRequest(
        UUID layoutPositionId,
        UUID studentId
) {
}