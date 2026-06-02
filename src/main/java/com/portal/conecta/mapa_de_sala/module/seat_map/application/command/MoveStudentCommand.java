package com.portal.conecta.mapa_de_sala.module.seat_map.application.command;

import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.MoveStudentRequest;
import java.util.UUID;

public record MoveStudentCommand(
        UUID roomMapId,
        MoveStudentRequest data
) {
}