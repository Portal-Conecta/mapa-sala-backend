package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case.RecordRoomMapHistoryUseCase;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomMapReplicationService {

    private final RoomLayoutRepository roomLayoutRepository;
    private final RoomMapRepository roomMapRepository;
    private final RecordRoomMapHistoryUseCase recordRoomMapHistoryUseCase;
    private final RequestContextProvider requestContextProvider;
    private final PlatformTransactionManager transactionManager;

    public void replicateToCompatibleRooms(RoomMap sourceMap, List<RoomMapLocation> sourceLocations) {
        UUID userId = requestContextProvider.getRequestContext().userId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doReplicateToCompatibleRooms(sourceMap, sourceLocations, userId);
                }
            });
            return;
        }

        doReplicateToCompatibleRooms(sourceMap, sourceLocations, userId);
    }

    private void doReplicateToCompatibleRooms(RoomMap sourceMap, List<RoomMapLocation> sourceLocations, UUID userId) {
        try {
            List<UUID> replicatedRoomIds = new ArrayList<>();
            UUID layoutTemplateId = sourceMap.getLayoutTemplateId();
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            for (RoomLayout roomLayout : roomLayoutRepository.findByLayoutTemplateId(layoutTemplateId)) {
                UUID destinationRoomId = roomLayout.getRoomId();

                if (sourceMap.getRoomId().equals(destinationRoomId)) {
                    continue;
                }

                try {
                    boolean replicated = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                        if (roomMapRepository
                                .findByClassIdAndRoomIdAndRemovedAtIsNull(sourceMap.getClassId(), destinationRoomId)
                                .isPresent()) {
                            return false;
                        }

                        RoomMap clonedMap = cloneRoomMap(sourceMap, sourceLocations, destinationRoomId);
                        RoomMap savedClone = roomMapRepository.save(clonedMap);

                        recordRoomMapHistoryUseCase.record(
                                savedClone.getId(),
                                RoomMapHistoryAction.MAP_REPLICATED.name(),
                                userId,
                                "Mapa criado por replicação a partir do mapa " + sourceMap.getId() + "."
                        );

                        return true;
                    }));

                    if (replicated) {
                        replicatedRoomIds.add(destinationRoomId);
                    }
                } catch (Exception ex) {
                    log.warn("Failed to replicate room map to roomId={}", destinationRoomId, ex);
                }
            }

            recordSourceHistoryWhenNeeded(sourceMap, replicatedRoomIds, userId, transactionTemplate);
        } catch (Exception ex) {
            log.warn("Failed to replicate room map from sourceRoomMapId={}", sourceMap.getId(), ex);
        }
    }

    private RoomMap cloneRoomMap(RoomMap sourceMap, List<RoomMapLocation> sourceLocations, UUID destinationRoomId) {
        RoomMap clonedMap = new RoomMap();
        clonedMap.setClassId(sourceMap.getClassId());
        clonedMap.setRoomId(destinationRoomId);
        clonedMap.setLayoutTemplateId(sourceMap.getLayoutTemplateId());
        clonedMap.setLayoutTemplate(sourceMap.getLayoutTemplate());

        if (sourceLocations != null) {
            sourceLocations.forEach(sourceLocation -> {
                RoomMapLocation clonedLocation = new RoomMapLocation();
                clonedLocation.setStudentId(sourceLocation.getStudentId());
                clonedLocation.setLayoutPositionId(sourceLocation.getLayoutPositionId());
                clonedLocation.setLayoutPosition(sourceLocation.getLayoutPosition());
                clonedLocation.setRoomMap(clonedMap);
                clonedMap.getLocations().add(clonedLocation);
            });
        }

        return clonedMap;
    }

    private void recordSourceHistoryWhenNeeded(
            RoomMap sourceMap,
            List<UUID> replicatedRoomIds,
            UUID userId,
            TransactionTemplate transactionTemplate
    ) {
        if (replicatedRoomIds.isEmpty()) {
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status -> recordRoomMapHistoryUseCase.record(
                    sourceMap.getId(),
                    RoomMapHistoryAction.MAP_REPLICATED.name(),
                    userId,
                    "Mapa replicado para " + replicatedRoomIds.size() + " sala(s): " + replicatedRoomIds + "."
            ));
        } catch (Exception ex) {
            log.warn("Failed to record replication history for sourceRoomMapId={}", sourceMap.getId(), ex);
        }
    }
}
