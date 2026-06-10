package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Resumo de um mapa de sala")
public record RoomMapSummaryResponse(
        @Schema(description = "Identificador do mapa de sala")
        UUID id,
        @Schema(description = "Identificador da turma")
        UUID classId,
        @Schema(description = "Identificador da sala")
        UUID salaId,
        @Schema(description = "Data de criação")
        LocalDateTime createdAt,
        @Schema(description = "Data da última atualização")
        LocalDateTime updatedAt
) {
}
