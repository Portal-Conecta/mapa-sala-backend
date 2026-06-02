package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateLayoutTemplateRequest(
        @Size(max = 255)
        String name,
        @Min(1)
        Integer dimensionX,
        @Min(1)
        Integer dimensionY,
        Boolean active
) {
}
