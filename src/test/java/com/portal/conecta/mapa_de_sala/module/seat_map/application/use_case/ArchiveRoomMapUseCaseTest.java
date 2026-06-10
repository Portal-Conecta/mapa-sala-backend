package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveRoomMapUseCaseTest {

    @Mock
    private RoomMapRepository roomMapRepository;

    @Mock
    private RequestContextProvider requestContextProvider;

    private ArchiveRoomMapUseCase useCase;

    private final UUID mapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID classId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID userId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        useCase = new ArchiveRoomMapUseCase(roomMapRepository, requestContextProvider);
    }

    @Test
    void execute_shouldArchiveWhenUserIsAdmin() {
        var roomMap = activeRoomMap();
        when(roomMapRepository.findById(mapId)).thenReturn(Optional.of(roomMap));
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.ADMIN, List.of()));

        useCase.execute(mapId);

        var saved = captureSavedRoomMap();
        assertThat(saved.getRemovedAt()).isNotNull();
        assertThat(saved.getRemovedBy()).isEqualTo(userId);
        assertThat(saved.isActive()).isFalse();
    }

    @Test
    void execute_shouldArchiveWhenTeacherIsLinkedToClass() {
        var roomMap = activeRoomMap();
        when(roomMapRepository.findById(mapId)).thenReturn(Optional.of(roomMap));
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(
                        userId,
                        TypeUser.TEACHER,
                        List.of(new ContextClass(classId, "TEACHER"))
                ));

        useCase.execute(mapId);

        var saved = captureSavedRoomMap();
        assertThat(saved.getRemovedAt()).isNotNull();
        assertThat(saved.getRemovedBy()).isEqualTo(userId);
    }

    @Test
    void execute_shouldThrowWhenTeacherIsNotLinkedToClass() {
        var roomMap = activeRoomMap();
        var otherClassId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        when(roomMapRepository.findById(mapId)).thenReturn(Optional.of(roomMap));
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(
                        userId,
                        TypeUser.TEACHER,
                        List.of(new ContextClass(otherClassId, "TEACHER"))
                ));

        assertThatThrownBy(() -> useCase.execute(mapId))
                .isInstanceOf(UnauthorizedUserException.class)
                .hasMessageContaining("não autorizado");

        verify(roomMapRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenStudentTriesToArchive() {
        var roomMap = activeRoomMap();
        when(roomMapRepository.findById(mapId)).thenReturn(Optional.of(roomMap));
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(
                        userId,
                        TypeUser.STUDENT,
                        List.of(new ContextClass(classId, "STUDENT"))
                ));

        assertThatThrownBy(() -> useCase.execute(mapId))
                .isInstanceOf(UnauthorizedUserException.class);

        verify(roomMapRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenMapNotFound() {
        when(roomMapRepository.findById(mapId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(mapId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mapa de sala");

        verify(roomMapRepository, never()).save(any());
    }

    @Test
    void isUserAuthorizedToArchiveRoomMap_shouldReturnTrueForAdmin() {
        var roomMap = activeRoomMap();
        var user = new RequestContext(userId, TypeUser.ADMIN, List.of());

        assertThat(useCase.isUserAuthorizedToArchiveRoomMap(user, roomMap)).isTrue();
    }

    @Test
    void isUserAuthorizedToArchiveRoomMap_shouldReturnTrueForLinkedTeacher() {
        var roomMap = activeRoomMap();
        var user = new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, "TEACHER"))
        );

        assertThat(useCase.isUserAuthorizedToArchiveRoomMap(user, roomMap)).isTrue();
    }

    @Test
    void isUserAuthorizedToArchiveRoomMap_shouldReturnFalseForUnlinkedTeacher() {
        var roomMap = activeRoomMap();
        var user = new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(UUID.randomUUID(), "TEACHER"))
        );

        assertThat(useCase.isUserAuthorizedToArchiveRoomMap(user, roomMap)).isFalse();
    }

    private RoomMap activeRoomMap() {
        var roomMap = new RoomMap();
        roomMap.setId(mapId);
        roomMap.setClassId(classId);
        roomMap.setRoomId(UUID.randomUUID());
        return roomMap;
    }

    private RoomMap captureSavedRoomMap() {
        var captor = ArgumentCaptor.forClass(RoomMap.class);
        verify(roomMapRepository).save(captor.capture());
        return captor.getValue();
    }
}
