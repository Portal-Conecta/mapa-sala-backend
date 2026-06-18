package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapSummaryResponse;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRoomMapsUseCaseTest {

    @Mock
    private RoomMapRepository roomMapRepository;

    @Mock
    private HubPermissionPort hubPermissionPort;

    @Mock
    private HubClassPort hubClassPort;

    private ListRoomMapsUseCase useCase;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID classId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID otherClassId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        useCase = new ListRoomMapsUseCase(roomMapRepository, hubPermissionPort, hubClassPort);
    }

    @Test
    void execute_aprendizShouldReceiveOnlyMapsFromOwnClass() {
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(classId);

        RoomMap roomMap = roomMap(classId);
        when(roomMapRepository.findByClassIdInAndRemovedAtIsNull(List.of(classId), pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap)));

        Page<?> result = useCase.execute(userId, TypeUser.STUDENT, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(hubClassPort).getClassIdForUser(userId);
        verify(roomMapRepository).findByClassIdInAndRemovedAtIsNull(List.of(classId), pageable);
    }

    @Test
    void execute_representanteShouldReceiveOnlyMapsFromOwnClass() {
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(classId);

        RoomMap roomMap = roomMap(classId);
        when(roomMapRepository.findByClassIdInAndRemovedAtIsNull(List.of(classId), pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap)));

        Page<?> result = useCase.execute(userId, TypeUser.REPRESENTATIVE, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(hubClassPort).getClassIdForUser(userId);
        verify(roomMapRepository).findByClassIdInAndRemovedAtIsNull(List.of(classId), pageable);
    }

    @Test
    void execute_perfilSenaiShouldReceiveAllNonArchivedMaps() {
        RoomMap roomMap1 = roomMap(classId);
        RoomMap roomMap2 = roomMap(otherClassId);
        when(roomMapRepository.findAllByRemovedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap1, roomMap2)));

        Page<?> result = useCase.execute(userId, TypeUser.SENAI, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(roomMapRepository).findAllByRemovedAtIsNull(pageable);
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"WEG", "ADMIN"})
    void execute_wegEAdminShouldReceiveAllNonArchivedMaps(TypeUser type) {
        RoomMap roomMap1 = roomMap(classId);
        RoomMap roomMap2 = roomMap(otherClassId);
        when(roomMapRepository.findAllByRemovedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap1, roomMap2)));

        Page<?> result = useCase.execute(userId, type, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(roomMapRepository).findAllByRemovedAtIsNull(pageable);
    }

    @Test
    void execute_docenteShouldReceiveOnlyLinkedClasses() {
        List<UUID> accessibleClassIds = List.of(classId, otherClassId);
        when(hubPermissionPort.getAccessibleClassIds(userId, TypeUser.TEACHER)).thenReturn(accessibleClassIds);

        RoomMap roomMap1 = roomMap(classId);
        RoomMap roomMap2 = roomMap(otherClassId);
        when(roomMapRepository.findByClassIdInAndRemovedAtIsNull(accessibleClassIds, pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap1, roomMap2)));

        Page<?> result = useCase.execute(userId, TypeUser.TEACHER, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        verify(hubPermissionPort).getAccessibleClassIds(userId, TypeUser.TEACHER);
        verify(roomMapRepository).findByClassIdInAndRemovedAtIsNull(accessibleClassIds, pageable);
    }

    @Test
    void execute_aprendizWithSalaIdShouldFilterByRoom() {
        UUID salaId = UUID.randomUUID();
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(classId);

        RoomMap roomMap = roomMap(classId);
        when(roomMapRepository.findByClassIdInAndRoomIdAndRemovedAtIsNull(List.of(classId), salaId, pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap)));

        Page<?> result = useCase.execute(userId, TypeUser.STUDENT, salaId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(roomMapRepository).findByClassIdInAndRoomIdAndRemovedAtIsNull(List.of(classId), salaId, pageable);
    }

    @Test
    void execute_perfilSenaiWithSalaIdShouldFilterByRoom() {
        UUID salaId = UUID.randomUUID();
        RoomMap roomMap = roomMap(classId);
        when(roomMapRepository.findAllByRemovedAtIsNullAndRoomId(salaId, pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap)));

        Page<?> result = useCase.execute(userId, TypeUser.SENAI, salaId, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(roomMapRepository).findAllByRemovedAtIsNullAndRoomId(salaId, pageable);
    }

    @Test
    void execute_shouldMapRoomMapFieldsToSummaryResponse() {
        RoomMap roomMap = roomMap(classId);

        when(roomMapRepository.findAllByRemovedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(roomMap)));

        Page<RoomMapSummaryResponse> result = useCase.execute(userId, TypeUser.SENAI, null, pageable);

        RoomMapSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.id()).isEqualTo(roomMap.getId());
        assertThat(summary.classId()).isEqualTo(roomMap.getClassId());
        assertThat(summary.salaId()).isEqualTo(roomMap.getRoomId());
    }

    private RoomMap roomMap(UUID classId) {
        RoomMap roomMap = new RoomMap();
        roomMap.setId(UUID.randomUUID());
        roomMap.setClassId(classId);
        roomMap.setRoomId(UUID.randomUUID());
        return roomMap;
    }
}