package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.MoveStudentCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.MoveConflictStrategy;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.InvalidLayoutPositionTypeException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.RoomMapAlreadyArchivedException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapLocationRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.MoveStudentRequest;
import com.portal.conecta.mapa_de_sala.shared.context.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveStudentUseCaseTest {

    @Mock private RoomMapRepository roomMapRepository;
    @Mock private RoomMapLocationRepository roomMapLocationRepository;
    @Mock private LayoutPositionRepository layoutPositionRepository;
    @Mock private RoomMapHistoryRepository roomMapHistoryRepository;
    @Mock private RequestContextProvider requestContextProvider;

    private MoveStudentUseCase useCase;

    private final UUID roomMapId   = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID classId     = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID userId      = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID studentId   = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private final UUID occupantId  = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private final UUID positionId  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID position2Id = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        useCase = new MoveStudentUseCase(
                roomMapRepository,
                roomMapLocationRepository,
                layoutPositionRepository,
                roomMapHistoryRepository,
                requestContextProvider
        );
    }

    // --- move simples ---

    @Test
    void execute_shouldMoveStudentToFreePosition() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(studentPosition(positionId)));
        when(roomMapLocationRepository.findByRoomMapIdAndStudentId(roomMapId, studentId))
                .thenReturn(Optional.of(studentLocation()));
        when(roomMapLocationRepository.findByRoomMapIdAndLayoutPositionId(roomMapId, positionId))
                .thenReturn(Optional.empty());

        useCase.execute(command(null));

        verify(roomMapLocationRepository).save(any(RoomMapLocation.class));
        verifyHistory(RoomMapHistoryAction.STUDENT_MOVED);
    }

    @Test
    void execute_shouldDefaultToDisplaceWhenOnConflictIsNull() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(studentPosition(positionId)));
        when(roomMapLocationRepository.findByRoomMapIdAndStudentId(roomMapId, studentId))
                .thenReturn(Optional.of(studentLocation()));
        when(roomMapLocationRepository.findByRoomMapIdAndLayoutPositionId(roomMapId, positionId))
                .thenReturn(Optional.of(occupantLocation()));

        useCase.execute(command(null)); // onConflict null → DISPLACE

        verify(roomMapLocationRepository).delete(any(RoomMapLocation.class));
        verifyHistory(RoomMapHistoryAction.STUDENT_UNASSIGNED);
        verifyHistory(RoomMapHistoryAction.STUDENT_MOVED);
    }

    // --- DISPLACE ---

    @Test
    void execute_shouldDisplaceOccupantAndMoveStudent() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(studentPosition(positionId)));
        when(roomMapLocationRepository.findByRoomMapIdAndStudentId(roomMapId, studentId))
                .thenReturn(Optional.of(studentLocation()));
        when(roomMapLocationRepository.findByRoomMapIdAndLayoutPositionId(roomMapId, positionId))
                .thenReturn(Optional.of(occupantLocation()));

        useCase.execute(command(MoveConflictStrategy.DISPLACE));

        verify(roomMapLocationRepository).delete(any(RoomMapLocation.class));
        verify(roomMapLocationRepository).save(any(RoomMapLocation.class));
        verifyHistory(RoomMapHistoryAction.STUDENT_UNASSIGNED);
        verifyHistory(RoomMapHistoryAction.STUDENT_MOVED);
    }

    // --- SWAP ---

    @Test
    void execute_shouldSwapStudentAndOccupant() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(studentPosition(positionId)));
        when(roomMapLocationRepository.findByRoomMapIdAndStudentId(roomMapId, studentId))
                .thenReturn(Optional.of(studentLocation()));
        when(roomMapLocationRepository.findByRoomMapIdAndLayoutPositionId(roomMapId, positionId))
                .thenReturn(Optional.of(occupantLocation()));

        useCase.execute(command(MoveConflictStrategy.SWAP));

        verify(roomMapLocationRepository, times(2)).delete(any(RoomMapLocation.class));
        verify(roomMapLocationRepository).flush();
        verify(roomMapLocationRepository, times(2)).save(any(RoomMapLocation.class));
        verifyHistory(RoomMapHistoryAction.STUDENTS_SWAPPED);
    }

    // --- validações ---

    @Test
    void execute_shouldThrowWhenMapNotFound() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mapa de sala");

        verify(roomMapLocationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenMapIsArchived() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(archivedRoomMap()));

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(RoomMapAlreadyArchivedException.class);

        verify(roomMapLocationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenTeacherIsNotLinkedToClass() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(
                new RequestContext(userId, TypeUser.TEACHER,
                        List.of(new ContextClass(UUID.randomUUID(), ClassRole.TEACHER)))
        );

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(AccessDeniedException.class);

        verify(roomMapLocationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenPositionNotFound() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Posição de layout");

        verify(roomMapLocationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenPositionTypeIsNotStudent() {
        LayoutPosition teacherPosition = studentPosition(positionId);
        teacherPosition.setType(LayoutPositionType.TEACHER);

        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(teacherPosition));

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(InvalidLayoutPositionTypeException.class)
                .hasMessageContaining("TEACHER");

        verify(roomMapLocationRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowWhenStudentIsNotAllocated() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findById(positionId)).thenReturn(Optional.of(studentPosition(positionId)));
        when(roomMapLocationRepository.findByRoomMapIdAndStudentId(roomMapId, studentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Alocação do aprendiz");

        verify(roomMapLocationRepository, never()).save(any());
    }

    // --- helpers ---

    private MoveStudentCommand command(MoveConflictStrategy strategy) {
        return new MoveStudentCommand(roomMapId, new MoveStudentRequest(studentId, positionId, strategy));
    }

    private RoomMap activeRoomMap() {
        var map = new RoomMap();
        map.setId(roomMapId);
        map.setClassId(classId);
        map.setRoomId(UUID.randomUUID());
        return map;
    }

    private RoomMap archivedRoomMap() {
        var map = activeRoomMap();
        map.setRemovedAt(Instant.now());
        map.setRemovedBy(userId);
        return map;
    }

    private RequestContext teacherContext() {
        return new RequestContext(userId, TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));
    }

    private LayoutPosition studentPosition(UUID id) {
        var pos = new LayoutPosition();
        pos.setId(id);
        pos.setType(LayoutPositionType.STUDENT);
        return pos;
    }

    private RoomMapLocation studentLocation() {
        var loc = new RoomMapLocation();
        loc.setStudentId(studentId);
        loc.setLayoutPosition(studentPosition(position2Id));
        loc.setRoomMap(activeRoomMap());
        return loc;
    }

    private RoomMapLocation occupantLocation() {
        var loc = new RoomMapLocation();
        loc.setStudentId(occupantId);
        loc.setLayoutPosition(studentPosition(positionId));
        loc.setRoomMap(activeRoomMap());
        return loc;
    }

    private void verifyHistory(RoomMapHistoryAction action) {
        var captor = ArgumentCaptor.forClass(RoomMapHistory.class);
        verify(roomMapHistoryRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(h -> h.getAction() == action);
    }
}