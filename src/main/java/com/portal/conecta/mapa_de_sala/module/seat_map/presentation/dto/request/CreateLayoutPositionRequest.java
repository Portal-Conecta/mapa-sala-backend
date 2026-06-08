package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLayoutPositionRequest(
        @NotNull
        UUID layoutTemplateId,
        @NotNull @Min(0)
        Integer positionX,
        @NotNull @Min(0)
        Integer positionY,
        @NotNull
        LayoutPositionType type
) {
}