package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import jakarta.validation.constraints.Min;

public record UpdateLayoutPositionRequest(
        @Min(0)
        Integer positionX,
        @Min(0)
        Integer positionY,
        LayoutPositionType type
) {
}