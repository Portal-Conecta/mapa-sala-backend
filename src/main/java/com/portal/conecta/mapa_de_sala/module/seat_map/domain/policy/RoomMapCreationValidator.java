package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomMapCreationValidator {

    private final HubClassPort hubClassPort;
    private final HubRoomPort hubRoomPort;
    private final RoomMapRepository roomMapRepository;

    public void validatePreConditions(UUID classId, UUID roomId, RequestContext context) {
        if (!hubClassPort.existsById(classId)) {
            throw new ResourceNotFoundException("Turma", classId);
        }
        if (!hubRoomPort.existsById(roomId)) {
            throw new ResourceNotFoundException("Sala", roomId);
        }

        boolean isLinkedToClass = context.classes().stream()
                .anyMatch(c -> c.classId().equals(classId));
        if (!isLinkedToClass) {
            throw new AccessDeniedException("Docente não vinculado à turma.");
        }

        boolean activeMapExists = roomMapRepository
                .findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId)
                .isPresent();
        if (activeMapExists) {
            throw new ConflictException("Já existe um mapa ativo para esta turma e sala.");
        }
    }
}