package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomMapHistoryResponse(
        UUID id,
        UUID roomMapId,
        UUID userId,
        String action,
        String details,
        LocalDateTime createdAt
) {
}