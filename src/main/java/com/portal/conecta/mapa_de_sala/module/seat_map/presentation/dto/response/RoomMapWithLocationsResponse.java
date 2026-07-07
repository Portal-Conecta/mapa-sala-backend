package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import java.util.List;

public record RoomMapWithLocationsResponse(
        RoomMapResponse map,
        List<RoomMapLocationResponse> locations
) {
}