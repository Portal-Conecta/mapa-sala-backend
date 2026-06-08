package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;

import java.util.UUID;

public record LayoutPositionResponse(
        UUID id,
        UUID layoutTemplateId,
        Integer positionX,
        Integer positionY,
        LayoutPositionType type
) {
}