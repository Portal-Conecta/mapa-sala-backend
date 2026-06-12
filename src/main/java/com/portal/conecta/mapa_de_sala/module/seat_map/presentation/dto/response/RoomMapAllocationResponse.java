package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Aluno alocado a um assento no mapa de sala")
public record RoomMapAllocationResponse(
        UUID studentId,
        String studenteName,
        Integer seatNumber,
        UUID layoutPositionId
) {

    public RoomMapAllocationResponse{
        Objects.requireNonNull(studentId, "studentId can't be null");
        Objects.requireNonNull(studenteName, "studenteName can't be null");
        Objects.requireNonNull(seatNumber, "seatNumber can't be null");
        Objects.requireNonNull(layoutPositionId, "layoutPositionId can't be null");
    }
}
