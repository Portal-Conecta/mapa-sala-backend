package com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Aluno da turma ainda não alocado em nenhum assento.")
public record UnassignedStudentResponse(
        UUID studentId,
        String studentName
) {
    public UnassignedStudentResponse{
        Objects.requireNonNull(studentId, "studentId can't be null");
        Objects.requireNonNull(studentName, "studentName can't be null");
    }
}
