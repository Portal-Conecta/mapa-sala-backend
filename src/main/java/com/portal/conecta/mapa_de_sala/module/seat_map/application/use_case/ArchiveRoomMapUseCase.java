package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchiveRoomMapUseCase {


    private final RoomMapRepository roomMapRepository;
    private final RequestContextProvider requestContextProvider;
    
    public void execute(UUID id) {
        RoomMap roomMap = roomMapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mapa de sala", id));

        RequestContext user = requestContextProvider.getRequestContext();

        if (!isUserAuthorizedToArchiveRoomMap(user, roomMap)) {
            throw new UnauthorizedUserException("Usuário não autorizado para arquivar mapa de sala");
        }

        roomMap.setRemovedAt(Instant.now());
        roomMap.setRemovedBy(user.userId());
        roomMapRepository.save(roomMap);
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
