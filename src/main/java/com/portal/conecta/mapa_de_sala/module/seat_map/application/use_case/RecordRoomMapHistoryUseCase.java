package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RecordRoomMapHistoryUseCase {

    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final RoomMapRepository roomMapRepository;

    public RecordRoomMapHistoryUseCase(
            RoomMapHistoryRepository roomMapHistoryRepository,
            RoomMapRepository roomMapRepository
    ) {
        this.roomMapHistoryRepository = roomMapHistoryRepository;
        this.roomMapRepository = roomMapRepository;
    }

    @Transactional
    public void record(UUID roomMapId, String action, UUID userId, String details) {
        RoomMap roomMap = roomMapRepository.getReferenceById(roomMapId);

        RoomMapHistory history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(userId);
        history.setAction(RoomMapHistoryAction.valueOf(action));
        history.setDetails(details);

        roomMapHistoryRepository.save(history);
    }
}
