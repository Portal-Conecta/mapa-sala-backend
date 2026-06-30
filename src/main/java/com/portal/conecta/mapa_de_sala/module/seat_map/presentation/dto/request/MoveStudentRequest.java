package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.MoveConflictStrategy;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveStudentRequest(
        @NotNull
        UUID studentId,
        @NotNull
        UUID targetLayoutPositionId,
        MoveConflictStrategy onConflict
) {
}