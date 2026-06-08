package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateRoomMapRequest(
        @NotNull
        UUID layoutTemplateId
) {
}