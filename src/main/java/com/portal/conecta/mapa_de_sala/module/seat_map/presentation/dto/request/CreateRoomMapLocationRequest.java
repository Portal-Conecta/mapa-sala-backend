package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRoomMapLocationRequest(
        @NotNull
        UUID roomMapId,
        @NotNull
        UUID layoutPositionId,
        @NotNull
        UUID studentId
) {
}