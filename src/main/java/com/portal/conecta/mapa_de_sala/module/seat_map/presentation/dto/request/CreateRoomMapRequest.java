package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateRoomMapRequest(
        @NotNull UUID classId,
        @NotNull UUID roomId,
        @NotNull UUID layoutTemplateId,
        List<CreateRoomMapInitialAllocationRequest> locations
) {}