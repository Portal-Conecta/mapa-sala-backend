package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLayoutTemplateRequest(
        @NotBlank @Size(max = 255)
        String name,
        @NotNull @Min(1)
        Integer dimensionX,
        @NotNull @Min(1)
        Integer dimensionY,
        boolean active
) {
}
