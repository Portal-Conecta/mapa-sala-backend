package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordRoomMapHistoryUseCaseTest {

    @Mock
    private RoomMapHistoryRepository roomMapHistoryRepository;

    @Mock
    private RoomMapRepository roomMapRepository;

    private RecordRoomMapHistoryUseCase useCase;

    private final UUID roomMapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        useCase = new RecordRoomMapHistoryUseCase(roomMapHistoryRepository, roomMapRepository);
    }

    @Test
    void record_shouldPersistHistoryEntry() {
        RoomMap roomMap = new RoomMap();
        when(roomMapRepository.getReferenceById(roomMapId)).thenReturn(roomMap);

        useCase.record(roomMapId, "MAP_CREATION", userId, "Mapa criado");

        ArgumentCaptor<RoomMapHistory> captor = ArgumentCaptor.forClass(RoomMapHistory.class);
        verify(roomMapHistoryRepository).save(captor.capture());

        RoomMapHistory saved = captor.getValue();
        assertThat(saved.getRoomMap()).isEqualTo(roomMap);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getAction()).isEqualTo(RoomMapHistoryAction.MAP_CREATION);
        assertThat(saved.getDetails()).isEqualTo("Mapa criado");
    }
}
