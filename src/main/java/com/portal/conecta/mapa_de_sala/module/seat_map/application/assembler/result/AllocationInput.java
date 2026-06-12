package com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.result;

import java.util.Objects;
import java.util.UUID;

public record AllocationInput(
        UUID studentId,
        String studentName,
        UUID layoutPositionId
) {
    public AllocationInput{
        Objects.requireNonNull(studentId, "studentId cannot be null");
        Objects.requireNonNull(layoutPositionId, "layoutPositionId cannot be null");
        Objects.requireNonNull(studentName, "studentName cannot be null");
    }
}
