package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Posição (assento, obstaculo...) dentro do grid do mapa de sala")
public record RoomMapGridPositionResponse(
        UUID layoutPositionId,
        Integer seatNumber,
        int positionX,
        int positionY,
        LayoutPositionType type
) {

    public RoomMapGridPositionResponse{
        Objects.requireNonNull(layoutPositionId, "layoutPosition cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        // seatNumber is intentionally nullable: not-STUDENT postions don't receive a number
    }
}
