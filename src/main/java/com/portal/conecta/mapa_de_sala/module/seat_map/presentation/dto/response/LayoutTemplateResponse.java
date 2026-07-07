package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import java.util.UUID;

public record LayoutTemplateResponse(
        UUID id,
        String name,
        Integer dimensionX,
        Integer dimensionY,
        boolean active
) {
}