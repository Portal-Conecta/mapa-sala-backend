package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapHistoryMapper;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListRoomMapHistoryUseCase {

    private final RoomMapRepository roomMapRepository;
    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final RoomMapHistoryMapper roomMapHistoryMapper;
    private final HubPermissionPort hubPermissionPort;
    private final HubClassPort hubClassPort;

    public ListRoomMapHistoryUseCase(
            RoomMapRepository roomMapRepository,
            RoomMapHistoryRepository roomMapHistoryRepository,
            RoomMapHistoryMapper roomMapHistoryMapper,
            HubPermissionPort hubPermissionPort,
            HubClassPort hubClassPort
    ) {
        this.roomMapRepository = roomMapRepository;
        this.roomMapHistoryRepository = roomMapHistoryRepository;
        this.roomMapHistoryMapper = roomMapHistoryMapper;
        this.hubPermissionPort = hubPermissionPort;
        this.hubClassPort = hubClassPort;
    }

    @Transactional(readOnly = true)
    public Page<RoomMapHistoryResponse> execute(UUID userId, TypeUser userType, UUID roomMapId, Pageable pageable) {
        RoomMap roomMap = roomMapRepository.findById(roomMapId)
                .filter(RoomMap::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Mapa de sala", roomMapId));

        if (!hasAccess(userId, userType, roomMap)) {
            throw new AccessDeniedException("Acesso negado ao mapa solicitado");
        }

        return roomMapHistoryRepository.findByRoomMapIdOrderByCreatedAtDesc(roomMapId, pageable)
                .map(roomMapHistoryMapper::toResponse);
    }

    private boolean hasAccess(UUID userId, TypeUser userType, RoomMap roomMap) {
        return switch (userType) {
            case STUDENT, REPRESENTATIVE -> hubClassPort.getClassIdForUser(userId).equals(roomMap.getClassId());
            case TEACHER -> hubPermissionPort.getAccessibleClassIds(userId, userType).contains(roomMap.getClassId());
            case SENAI, WEG, ADMIN -> true;
        };
    }
}
