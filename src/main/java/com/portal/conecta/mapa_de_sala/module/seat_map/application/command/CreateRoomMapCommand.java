package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import java.util.List;
import java.util.UUID;

public record CreateRoomMapCommand(
        UUID classId,
        UUID roomId,
        UUID layoutTemplateId,
        List<CreateRoomMapInitialAllocationCommand> locations
) {}