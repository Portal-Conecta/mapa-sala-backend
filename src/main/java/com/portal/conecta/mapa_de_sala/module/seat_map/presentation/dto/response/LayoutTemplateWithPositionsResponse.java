package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Template de layout com dimensões e posições da sala")
public record LayoutTemplateWithPositionsResponse(
        @Schema(description = "Identificador do template de layout")
        UUID layoutTemplateId,
        @Schema(description = "Largura do grid", example = "10")
        Integer dimensionX,
        @Schema(description = "Altura do grid", example = "10")
        Integer dimensionY,
        @Schema(description = "Posições ordenadas por positionY ASC, positionX ASC")
        List<LayoutPositionItemResponse> positions
) {
}
