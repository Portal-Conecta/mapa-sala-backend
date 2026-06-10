package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.dto;

import java.util.UUID;

public record HubUserResponse(
        UUID id,
        String name
) {
}
