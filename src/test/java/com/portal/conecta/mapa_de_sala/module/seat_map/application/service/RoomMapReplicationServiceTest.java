package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.RecordRoomMapHistoryUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutTemplate;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomMapReplicationServiceTest {

    @Mock
    private RoomLayoutRepository roomLayoutRepository;

    @Mock
    private RoomMapRepository roomMapRepository;

    @Mock
    private RecordRoomMapHistoryUseCase recordRoomMapHistoryUseCase;

    @Mock
    private RequestContextProvider requestContextProvider;

    @Mock
    private PlatformTransactionManager transactionManager;

    private RoomMapReplicationService service;

    private UUID classId;
    private UUID sourceRoomId;
    private UUID sourceMapId;
    private UUID templateId;
    private UUID userId;
    private LayoutTemplate template;
    private RoomMap sourceMap;
    private List<RoomMapLocation> sourceLocations;

    @BeforeEach
    void setUp() {
        service = new RoomMapReplicationService(
                roomLayoutRepository,
                roomMapRepository,
                recordRoomMapHistoryUseCase,
                requestContextProvider,
                transactionManager
        );

        classId = UUID.randomUUID();
        sourceRoomId = UUID.randomUUID();
        sourceMapId = UUID.randomUUID();
        templateId = UUID.randomUUID();
        userId = UUID.randomUUID();

        template = new LayoutTemplate();
        template.setId(templateId);

        sourceMap = new RoomMap();
        sourceMap.setId(sourceMapId);
        sourceMap.setClassId(classId);
        sourceMap.setRoomId(sourceRoomId);
        sourceMap.setLayoutTemplateId(templateId);
        sourceMap.setLayoutTemplate(template);

        sourceLocations = List.of(
                location(UUID.randomUUID(), UUID.randomUUID()),
                location(UUID.randomUUID(), UUID.randomUUID())
        );

        lenient().when(transactionManager.getTransaction(any()))
                .thenReturn(new SimpleTransactionStatus());
        lenient().when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
    }

    @Test
    void eligibleRoomReceivesClone() {
        UUID destinationRoomId = UUID.randomUUID();
        UUID cloneId = UUID.randomUUID();
        when(roomLayoutRepository.findByLayoutTemplateId(templateId))
                .thenReturn(List.of(roomLayout(sourceRoomId), roomLayout(destinationRoomId)));
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, destinationRoomId))
                .thenReturn(Optional.empty());
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
        doAnswer(invocation -> {
            RoomMap saved = invocation.getArgument(0);
            saved.setId(cloneId);
            return saved;
        }).when(roomMapRepository).save(any(RoomMap.class));

        service.replicateToCompatibleRooms(sourceMap, sourceLocations);

        ArgumentCaptor<RoomMap> mapCaptor = ArgumentCaptor.forClass(RoomMap.class);
        verify(roomMapRepository).save(mapCaptor.capture());
        RoomMap clone = mapCaptor.getValue();

        assertThat(clone.getClassId()).isEqualTo(classId);
        assertThat(clone.getRoomId()).isEqualTo(destinationRoomId);
        assertThat(clone.getLayoutTemplateId()).isEqualTo(templateId);
        assertThat(clone.getLayoutTemplate()).isEqualTo(template);
        assertThat(clone.getLocations()).hasSize(2);
        assertThat(clone.getLocations())
                .extracting(RoomMapLocation::getStudentId)
                .containsExactly(sourceLocations.get(0).getStudentId(), sourceLocations.get(1).getStudentId());
        assertThat(clone.getLocations())
                .extracting(RoomMapLocation::getLayoutPositionId)
                .containsExactly(sourceLocations.get(0).getLayoutPositionId(), sourceLocations.get(1).getLayoutPositionId());
        assertThat(clone.getLocations())
                .allSatisfy(location -> assertThat(location.getRoomMap()).isEqualTo(clone));

        verify(recordRoomMapHistoryUseCase).record(
                cloneId,
                RoomMapHistoryAction.MAP_REPLICATED.name(),
                userId,
                "Mapa criado por replicação a partir do mapa " + sourceMapId + "."
        );
        verify(recordRoomMapHistoryUseCase).record(
                sourceMapId,
                RoomMapHistoryAction.MAP_REPLICATED.name(),
                userId,
                "Mapa replicado para 1 sala(s): [" + destinationRoomId + "]."
        );
    }

    @Test
    void roomWithExistingActiveMapIsSkipped() {
        UUID destinationRoomId = UUID.randomUUID();
        when(roomLayoutRepository.findByLayoutTemplateId(templateId))
                .thenReturn(List.of(roomLayout(destinationRoomId)));
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, destinationRoomId))
                .thenReturn(Optional.of(new RoomMap()));

        service.replicateToCompatibleRooms(sourceMap, sourceLocations);

        verify(roomMapRepository, never()).save(any(RoomMap.class));
        verify(recordRoomMapHistoryUseCase, never()).record(any(), any(), any(), any());
    }

    @Test
    void roomWithDifferentTemplateIsIgnored() {
        UUID differentTemplateId = UUID.randomUUID();
        when(roomLayoutRepository.findByLayoutTemplateId(templateId)).thenReturn(List.of());

        service.replicateToCompatibleRooms(sourceMap, sourceLocations);

        verify(roomLayoutRepository).findByLayoutTemplateId(templateId);
        verify(roomLayoutRepository, never()).findByLayoutTemplateId(differentTemplateId);
        verify(roomMapRepository, never()).save(any(RoomMap.class));
    }

    @Test
    void failureWhileCloningOneRoomDoesNotInterruptRemainingReplications() {
        UUID failingRoomId = UUID.randomUUID();
        UUID successfulRoomId = UUID.randomUUID();
        UUID successfulCloneId = UUID.randomUUID();
        when(roomLayoutRepository.findByLayoutTemplateId(templateId))
                .thenReturn(List.of(roomLayout(failingRoomId), roomLayout(successfulRoomId)));
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(eq(classId), any(UUID.class)))
                .thenReturn(Optional.empty());
        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(userId, TypeUser.TEACHER, List.of()));
        doThrow(new RuntimeException("save failed"))
                .doAnswer(invocation -> {
                    RoomMap saved = invocation.getArgument(0);
                    saved.setId(successfulCloneId);
                    return saved;
                })
                .when(roomMapRepository).save(any(RoomMap.class));

        service.replicateToCompatibleRooms(sourceMap, sourceLocations);

        ArgumentCaptor<RoomMap> mapCaptor = ArgumentCaptor.forClass(RoomMap.class);
        verify(roomMapRepository, org.mockito.Mockito.times(2)).save(mapCaptor.capture());
        assertThat(mapCaptor.getAllValues())
                .extracting(RoomMap::getRoomId)
                .containsExactly(failingRoomId, successfulRoomId);
        verify(recordRoomMapHistoryUseCase).record(
                successfulCloneId,
                RoomMapHistoryAction.MAP_REPLICATED.name(),
                userId,
                "Mapa criado por replicação a partir do mapa " + sourceMapId + "."
        );
        verify(recordRoomMapHistoryUseCase).record(
                sourceMapId,
                RoomMapHistoryAction.MAP_REPLICATED.name(),
                userId,
                "Mapa replicado para 1 sala(s): [" + successfulRoomId + "]."
        );
    }

    private RoomLayout roomLayout(UUID roomId) {
        RoomLayout roomLayout = new RoomLayout();
        roomLayout.setRoomId(roomId);
        roomLayout.setLayoutTemplateId(templateId);
        roomLayout.setLayoutTemplate(template);
        return roomLayout;
    }

    private RoomMapLocation location(UUID studentId, UUID layoutPositionId) {
        RoomMapLocation location = new RoomMapLocation();
        location.setStudentId(studentId);
        location.setLayoutPositionId(layoutPositionId);
        return location;
    }
}
