package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.CreateRoomMapRequest;
import java.util.List;
import java.util.UUID;

public record CreateRoomMapCommand(
        UUID classId,
        UUID roomId,
        UUID layoutTemplateId,
        List<CreateRoomMapInitialAllocationCommand> locations
) {
    public CreateRoomMapCommand(CreateRoomMapRequest request) {
        this(
                request.classId(),
                request.roomId(),
                request.layoutTemplateId(),
                request.locations() == null ? List.of() : request.locations().stream()
                        .map(loc -> new CreateRoomMapInitialAllocationCommand(
                                loc.studentId(),
                                loc.seatNumber(),
                                loc.layoutPositionId()
                        )).toList()
        );
    }
}