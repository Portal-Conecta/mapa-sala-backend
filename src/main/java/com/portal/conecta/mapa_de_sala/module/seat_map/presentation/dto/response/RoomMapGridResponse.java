package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Grid do mapa de sala, com dimensões e posições.")
public record RoomMapGridResponse(
        int rows,
        int columns,
        int totalSeats,
        List<RoomMapGridPositionResponse> positions
) {
    public RoomMapGridResponse{
        Objects.requireNonNull(positions, "Positions can't be null");
        positions = List.copyOf(positions);
    }
}