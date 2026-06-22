package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRoomMapAllocationsRequest(
        @NotNull @NotEmpty
        @Valid List<AllocationEntryRequest> allocations
) {
}
