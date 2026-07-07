package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListRoomMapsUseCase {

    private final RoomMapRepository roomMapRepository;
    private final HubPermissionPort hubPermissionPort;
    private final HubClassPort hubClassPort;

    public ListRoomMapsUseCase(
            RoomMapRepository roomMapRepository,
            HubPermissionPort hubPermissionPort,
            HubClassPort hubClassPort
    ) {
        this.roomMapRepository = roomMapRepository;
        this.hubPermissionPort = hubPermissionPort;
        this.hubClassPort = hubClassPort;
    }

    @Transactional(readOnly = true)
    public Page<RoomMapSummaryResponse> execute(UUID userId, TypeUser userType, UUID salaId, Pageable pageable) {
        Page<RoomMap> page = switch (userType) {
            case STUDENT, REPRESENTATIVE -> findByClassIds(List.of(hubClassPort.getClassIdForUser(userId)), salaId, pageable);
            case TEACHER -> findByClassIds(hubPermissionPort.getAccessibleClassIds(userId, userType), salaId, pageable);
            case SENAI, WEG, ADMIN -> findAll(salaId, pageable);
        };

        return page.map(this::toSummaryResponse);
    }

    private Page<RoomMap> findByClassIds(List<UUID> classIds, UUID salaId, Pageable pageable) {
        if (salaId != null) {
            return roomMapRepository.findByClassIdInAndRoomIdAndRemovedAtIsNull(classIds, salaId, pageable);
        }
        return roomMapRepository.findByClassIdInAndRemovedAtIsNull(classIds, pageable);
    }

    private Page<RoomMap> findAll(UUID salaId, Pageable pageable) {
        if (salaId != null) {
            return roomMapRepository.findAllByRemovedAtIsNullAndRoomId(salaId, pageable);
        }
        return roomMapRepository.findAllByRemovedAtIsNull(pageable);
    }

    private RoomMapSummaryResponse toSummaryResponse(RoomMap roomMap) {
        return new RoomMapSummaryResponse(
                roomMap.getId(),
                roomMap.getClassId(),
                roomMap.getRoomId(),
                roomMap.getCreatedAt(),
                roomMap.getUpdatedAt()
        );
    }
}
