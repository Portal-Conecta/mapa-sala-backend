package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class RoomMapAllocationValidator {

    public void validate(
            List<UUID> studentIds,
            List<UUID> positionIds,
            List<LayoutPosition> templatePositions
    ) {
        Set<UUID> uniqueStudents = new HashSet<>();
        for (UUID studentId : studentIds) {
            if (!uniqueStudents.add(studentId)) {
                throw new ConflictException("O mesmo aprendiz não pode ocupar mais de uma posição: " + studentId);
            }
        }

        Set<UUID> uniquePositions = new HashSet<>();

        for (UUID positionId : positionIds) {
            if (!uniquePositions.add(positionId)) {
                throw new ConflictException("A mesma posição não pode ser ocupada por mais de um aprendiz: " + positionId);
            }
        }

        Set<UUID> validStudentPositionIds = new HashSet<>();

        for (LayoutPosition position : templatePositions) {
            if (position.getType() == LayoutPositionType.STUDENT) {
                validStudentPositionIds.add(position.getId());
            }
        }

        for (UUID positionId : positionIds) {
            boolean positionExistsInTemplate = templatePositions.stream().anyMatch(p -> p.getId().equals(positionId));
            if (!positionExistsInTemplate) {
                throw new BadRequestException("A posição não pertence ao template: " + positionId);
            }
            if (!validStudentPositionIds.contains(positionId)) {
                throw new BadRequestException("A posição não é do tipo STUDENT: " + positionId);
            }
        }
    }
}