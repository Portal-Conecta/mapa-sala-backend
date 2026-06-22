package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.RoomMapViewAssembler;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapAllocationsCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapAllocationsUpdateValidator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.LayoutPositionRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapLocationRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.AllocationEntryRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.UpdateRoomMapAllocationsRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateRoomMapAllocationsUseCaseTest {

    @Mock private RoomMapRepository roomMapRepository;
    @Mock private RoomMapLocationRepository roomMapLocationRepository;
    @Mock private LayoutPositionRepository layoutPositionRepository;
    @Mock private RoomMapHistoryRepository roomMapHistoryRepository;
    @Mock private HubClassPort hubClassPort;
    @Mock private RoomMapViewAssembler assembler;
    @Mock private RequestContextProvider requestContextProvider;
    @Mock private RoomMapAllocationsUpdateValidator allocationsUpdateValidator;

    private final SeatNumberCalculator seatNumberCalculator = new SeatNumberCalculator();

    private UpdateRoomMapAllocationsUseCase useCase;

    private final UUID roomMapId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID classId   = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID userId    = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID templateId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final UUID seat1      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID seat2      = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID student1   = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private final UUID student2   = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    @BeforeEach
    void setUp() {
        useCase = new UpdateRoomMapAllocationsUseCase(
                roomMapRepository,
                roomMapLocationRepository,
                layoutPositionRepository,
                roomMapHistoryRepository,
                hubClassPort,
                allocationsUpdateValidator,
                seatNumberCalculator,
                assembler,
                requestContextProvider
        );
    }

    @Test
    void execute_shouldUpdateAllocationsSuccessfullyForTeacher() {
        setupHappyPath();

        useCase.execute(command(entry(student1, seat1), entry(student2, seat2)));

        verify(roomMapLocationRepository).deleteByRoomMapId(roomMapId);
        verify(roomMapLocationRepository, times(2)).flush();

        var captor = ArgumentCaptor.forClass(List.class);
        verify(roomMapLocationRepository).saveAll(captor.capture());
        List<RoomMapLocation> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(RoomMapLocation::getStudentId)
                .containsExactlyInAnyOrder(student1, student2);
    }

    @Test
    void execute_shouldUpdateAllocationsSuccessfullyForAdmin() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.ADMIN, List.of()));
        when(layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(templatePositions());
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(classStudents());
        when(assembler.assembleFromSavedMap(any(), anyInt(), anyInt(), anyList(), any(), anyList(), anyList()))
                .thenReturn(mock(RoomMapViewResponse.class));

        useCase.execute(command(entry(student1, seat1), entry(student2, seat2)));

        verify(roomMapLocationRepository).saveAll(anyList());
    }

    @Test
    void execute_shouldSaveHistoryWithMapUpdatedAction() {
        setupHappyPath();

        useCase.execute(command(entry(student1, seat1), entry(student2, seat2)));

        var captor = ArgumentCaptor.forClass(RoomMapHistory.class);
        verify(roomMapHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(RoomMapHistoryAction.MAP_UPDATED);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getDetails()).contains("2 alunos alocados");
    }

    @Test
    void execute_shouldDelegateValidationToValidator() {
        setupHappyPath();

        useCase.execute(command(entry(student1, seat1), entry(student2, seat2)));

        verify(allocationsUpdateValidator).validate(anyList(), any(), any());
    }

    @Test
    void execute_shouldThrowWhenMapNotFound() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(entry(student1, seat1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mapa de sala");

        verify(roomMapLocationRepository, never()).deleteByRoomMapId(any());
    }

    @Test
    void execute_shouldThrowWhenMapIsArchived() {
        var roomMap = activeRoomMap();
        roomMap.setRemovedAt(Instant.now());
        roomMap.setRemovedBy(userId);
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(roomMap));

        assertThatThrownBy(() -> useCase.execute(command(entry(student1, seat1))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roomMapLocationRepository, never()).deleteByRoomMapId(any());
    }

    @Test
    void execute_shouldThrowWhenTeacherIsNotLinkedToClass() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(
                new RequestContext(userId, TypeUser.TEACHER,
                        List.of(new ContextClass(UUID.randomUUID(), "TEACHER")))
        );

        assertThatThrownBy(() -> useCase.execute(command(entry(student1, seat1))))
                .isInstanceOf(AccessDeniedException.class);

        verify(roomMapLocationRepository, never()).deleteByRoomMapId(any());
    }

    @Test
    void execute_shouldThrowWhenStudentTries() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(
                new RequestContext(userId, TypeUser.STUDENT, List.of(new ContextClass(classId, "STUDENT")))
        );

        assertThatThrownBy(() -> useCase.execute(command(entry(student1, seat1))))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void execute_shouldNotPersistWhenValidationFails() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(templatePositions());
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(classStudents());
        doThrow(new BadRequestException("validação falhou"))
                .when(allocationsUpdateValidator).validate(any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(command(entry(student1, seat1))))
                .isInstanceOf(BadRequestException.class);

        verify(roomMapLocationRepository, never()).deleteByRoomMapId(any());
    }

    // --- helpers ---

    private void setupHappyPath() {
        when(roomMapRepository.findById(roomMapId)).thenReturn(Optional.of(activeRoomMap()));
        when(requestContextProvider.getRequestContext()).thenReturn(teacherContext());
        when(layoutPositionRepository.findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(templateId))
                .thenReturn(templatePositions());
        when(hubClassPort.findStudentsByClassId(classId)).thenReturn(classStudents());
        when(assembler.assembleFromSavedMap(any(), anyInt(), anyInt(), anyList(), any(), anyList(), anyList()))
                .thenReturn(mock(RoomMapViewResponse.class));
    }

    private UpdateRoomMapAllocationsCommand command(AllocationEntryRequest... entries) {
        return new UpdateRoomMapAllocationsCommand(roomMapId, new UpdateRoomMapAllocationsRequest(List.of(entries)));
    }

    private AllocationEntryRequest entry(UUID studentId, UUID positionId) {
        return new AllocationEntryRequest(studentId, positionId);
    }

    private RequestContext teacherContext() {
        return new RequestContext(userId, TypeUser.TEACHER, List.of(new ContextClass(classId, "TEACHER")));
    }

    private RoomMap activeRoomMap() {
        var template = new LayoutTemplate();
        template.setId(templateId);
        template.setDimensionX(3);
        template.setDimensionY(1);
        template.setActive(true);

        var roomMap = new RoomMap();
        roomMap.setId(roomMapId);
        roomMap.setClassId(classId);
        roomMap.setRoomId(UUID.randomUUID());
        roomMap.setLayoutTemplateId(templateId);
        roomMap.setLayoutTemplate(template);
        return roomMap;
    }

    private List<LayoutPosition> templatePositions() {
        return List.of(
                position(seat1, 0, 0, LayoutPositionType.STUDENT),
                position(seat2, 1, 0, LayoutPositionType.STUDENT),
                position(UUID.fromString("44444444-4444-4444-4444-444444444444"), 2, 0, LayoutPositionType.TEACHER)
        );
    }

    private LayoutPosition position(UUID id, int x, int y, LayoutPositionType type) {
        var pos = new LayoutPosition();
        pos.setId(id);
        pos.setPositionX(x);
        pos.setPositionY(y);
        pos.setType(type);
        return pos;
    }

    private List<HubStudent> classStudents() {
        return List.of(
                new HubStudent(student1, "Ana Silva"),
                new HubStudent(student2, "Bruno Costa")
        );
    }
}