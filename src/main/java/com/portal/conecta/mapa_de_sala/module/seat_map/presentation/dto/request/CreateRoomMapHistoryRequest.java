package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoomMapHistoryRequest(
        @NotNull
        UUID roomMapId,
        @NotNull
        String action,
        @Size(max = 2000)
        String details
) {
}