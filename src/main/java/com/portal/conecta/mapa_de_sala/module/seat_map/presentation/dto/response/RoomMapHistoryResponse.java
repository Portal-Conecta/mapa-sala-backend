package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Registro de histórico de alterações em um mapa de sala")
public record RoomMapHistoryResponse(
        @Schema(description = "Identificador do registro de histórico")
        UUID id,
        @Schema(description = "Identificador do mapa de sala")
        UUID roomMapId,
        @Schema(description = "Identificador do usuário que realizou a ação")
        UUID userId,
        @Schema(description = "Ação registrada no histórico")
        String action,
        @Schema(description = "Detalhes da alteração")
        String details,
        @Schema(description = "Data de criação do registro")
        LocalDateTime createdAt
) {
}