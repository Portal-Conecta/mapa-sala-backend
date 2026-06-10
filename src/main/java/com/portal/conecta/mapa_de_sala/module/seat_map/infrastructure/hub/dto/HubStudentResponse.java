package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.dto;

import java.util.UUID;

public record HubStudentResponse(
        UUID id,
        String name
) {
}
