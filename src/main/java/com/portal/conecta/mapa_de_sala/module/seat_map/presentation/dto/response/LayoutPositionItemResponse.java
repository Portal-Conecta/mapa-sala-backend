package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Posição individual no grid do layout")
public record LayoutPositionItemResponse(
        @Schema(description = "Coordenada X no grid", example = "0")
        Integer positionX,
        @Schema(description = "Coordenada Y no grid", example = "1")
        Integer positionY,
        @Schema(description = "Tipo da posição", example = "STUDENT")
        LayoutPositionType type
) {
}
