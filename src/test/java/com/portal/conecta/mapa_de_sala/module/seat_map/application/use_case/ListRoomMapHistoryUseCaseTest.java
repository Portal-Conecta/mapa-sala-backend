package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapHistoryResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapHistoryMapper;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRoomMapHistoryUseCaseTest {

    @Mock
    private RoomMapRepository roomMapRepository;

    @Mock
    private RoomMapHistoryRepository roomMapHistoryRepository;

    @Mock
    private RoomMapHistoryMapper roomMapHistoryMapper;

    @Mock
    private HubPermissionPort hubPermissionPort;

    @Mock
    private HubClassPort hubClassPort;

    private ListRoomMapHistoryUseCase useCase;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID roomMapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID classId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        useCase = new ListRoomMapHistoryUseCase(
                roomMapRepository,
                roomMapHistoryRepository,
                roomMapHistoryMapper,
                hubPermissionPort,
                hubClassPort
        );
    }

    @Test
    void execute_aprendizWithAccessShouldReturnHistory() {
        RoomMap roomMap = activeRoomMap();
        RoomMapHistory history = historyEntry();
        RoomMapHistoryResponse response = historyResponse();

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(classId);
        when(roomMapHistoryRepository.findByRoomMapIdOrderByCreatedAtDesc(roomMapId, pageable))
                .thenReturn(new PageImpl<>(List.of(history)));
        when(roomMapHistoryMapper.toResponse(history)).thenReturn(response);

        Page<RoomMapHistoryResponse> result = useCase.execute(userId, TypeUser.STUDENT, roomMapId, pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(roomMapHistoryRepository).findByRoomMapIdOrderByCreatedAtDesc(roomMapId, pageable);
    }

    @Test
    void execute_docenteWithAccessShouldReturnHistory() {
        RoomMap roomMap = activeRoomMap();
        RoomMapHistory history = historyEntry();
        RoomMapHistoryResponse response = historyResponse();

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));
        when(hubPermissionPort.getAccessibleClassIds(userId, TypeUser.TEACHER)).thenReturn(List.of(classId));
        when(roomMapHistoryRepository.findByRoomMapIdOrderByCreatedAtDesc(roomMapId, pageable))
                .thenReturn(new PageImpl<>(List.of(history)));
        when(roomMapHistoryMapper.toResponse(history)).thenReturn(response);

        Page<RoomMapHistoryResponse> result = useCase.execute(userId, TypeUser.TEACHER, roomMapId, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void execute_shouldThrow404WhenMapNotFound() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, TypeUser.SENAI, roomMapId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldThrow404WhenMapIsArchived() {
        RoomMap roomMap = activeRoomMap();
        ReflectionTestUtils.setField(roomMap, "removedAt", Instant.now());

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));

        assertThatThrownBy(() -> useCase.execute(userId, TypeUser.SENAI, roomMapId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldThrow403WhenAprendizHasNoAccess() {
        RoomMap roomMap = activeRoomMap();

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.execute(userId, TypeUser.STUDENT, roomMapId, pageable))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void execute_shouldThrow403WhenDocenteHasNoAccess() {
        RoomMap roomMap = activeRoomMap();

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));
        when(hubPermissionPort.getAccessibleClassIds(userId, TypeUser.TEACHER)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(userId, TypeUser.TEACHER, roomMapId, pageable))
                .isInstanceOf(AccessDeniedException.class);
    }

    private RoomMap activeRoomMap() {
        RoomMap roomMap = new RoomMap();
        ReflectionTestUtils.setField(roomMap, "id", roomMapId);
        roomMap.setClassId(classId);
        roomMap.setRoomId(UUID.randomUUID());
        return roomMap;
    }

    private RoomMapHistory historyEntry() {
        RoomMapHistory history = new RoomMapHistory();
        ReflectionTestUtils.setField(history, "id", UUID.randomUUID());
        history.setRoomMap(activeRoomMap());
        history.setUserId(userId);
        history.setAction(RoomMapHistoryAction.MAP_CREATION);
        history.setDetails("Mapa criado");
        return history;
    }

    private RoomMapHistoryResponse historyResponse() {
        return new RoomMapHistoryResponse(
                UUID.randomUUID(),
                roomMapId,
                userId,
                "MAP_CREATION",
                "Mapa criado",
                Instant.now()
        );
    }
}
