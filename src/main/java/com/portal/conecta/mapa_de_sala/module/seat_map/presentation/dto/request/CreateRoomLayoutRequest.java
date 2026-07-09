package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateRoomLayoutRequest(
        @NotNull(message = "O identificador da sala é obrigatório.")
        UUID roomId,
        @NotNull(message = "O identificador do template é obrigatório.")
        UUID layoutTemplateId
) {
}