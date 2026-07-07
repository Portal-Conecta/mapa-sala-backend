package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import java.time.Instant;
import java.util.UUID;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.RoomMapAlreadyArchivedException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArchiveRoomMapUseCase {

    private final RoomMapRepository roomMapRepository;
    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final RequestContextProvider requestContextProvider;
    
    public void execute(UUID id) {
        RoomMap roomMap = roomMapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapa de sala", id));

        if (!roomMap.isActive()) {
            throw new RoomMapAlreadyArchivedException(id);
        }

        RequestContext user = requestContextProvider.getRequestContext();

        if (!isUserAuthorizedToArchiveRoomMap(user, roomMap)) {
            throw new AccessDeniedException("Usuário não autorizado para arquivar mapa de sala");
        }

        roomMap.setRemovedAt(Instant.now());
        roomMap.setRemovedBy(user.userId());

        roomMapRepository.save(roomMap);

        var history = new RoomMapHistory();

        history.setRoomMap(roomMap);
        history.setUserId(user.userId());
        history.setAction(RoomMapHistoryAction.MAP_ARCHIVED);

        roomMapHistoryRepository.save(history);
    }

    public boolean isUserAuthorizedToArchiveRoomMap(RequestContext user, RoomMap roomMap) {
        if(user.userType() == TypeUser.ADMIN) {
            return true;
        }

        if(user.userType() == TypeUser.TEACHER) {
            return user.classes()
                .stream()
                .anyMatch(c -> c.classId().equals(roomMap.getClassId()));
        }

        return false;
    }
}
