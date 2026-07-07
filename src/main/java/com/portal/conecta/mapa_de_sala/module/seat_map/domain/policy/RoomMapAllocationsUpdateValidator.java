package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.AllocationEntryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RoomMapAllocationsUpdateValidator {

    public void validate(
            List<AllocationEntryRequest> entries,
            Map<UUID, LayoutPosition> positionById,
            Set<UUID> classStudentIds
    ) {
        Set<UUID> seenStudents = new HashSet<>();
        Set<UUID> seenPositions = new HashSet<>();

        for (AllocationEntryRequest entry : entries) {
            if (!seenStudents.add(entry.studentId())) {
                throw new BadRequestException("studentId duplicado na lista: " + entry.studentId());
            }

            if (!seenPositions.add(entry.layoutPositionId())) {
                throw new BadRequestException("layoutPositionId duplicado na lista: " + entry.layoutPositionId());
            }

            LayoutPosition position = positionById.get(entry.layoutPositionId());
            if (position == null) {
                throw new BadRequestException(
                        "layoutPositionId não pertence ao template do mapa: " + entry.layoutPositionId());
            }

            if (position.getType() != LayoutPositionType.STUDENT) {
                throw new BadRequestException(
                        "layoutPositionId não é do tipo STUDENT: " + entry.layoutPositionId()
                                + " (tipo atual: " + position.getType() + ")");
            }

            if (!classStudentIds.contains(entry.studentId())) {
                throw new BadRequestException("studentId não pertence à turma do mapa: " + entry.studentId());
            }
        }

        if (!seenStudents.equals(classStudentIds)) {
            Set<UUID> missing = new HashSet<>(classStudentIds);
            missing.removeAll(seenStudents);
            throw new BadRequestException(
                    "Todos os alunos da turma devem estar alocados. Alunos ausentes: " + missing);
        }
    }
}