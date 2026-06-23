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

    public void replicateToCompatibleRooms(RoomMap sourceMap, List<RoomMapLocation> sourceLocations) {
        try {
            List<UUID> replicatedRoomIds = new ArrayList<>();
            UUID layoutTemplateId = sourceMap.getLayoutTemplateId();

            for (RoomLayout roomLayout : roomLayoutRepository.findByLayoutTemplateId(layoutTemplateId)) {
                UUID destinationRoomId = roomLayout.getRoomId();

                if (sourceMap.getRoomId().equals(destinationRoomId)) {
                    continue;
                }

                try {
                    if (roomMapRepository
                            .findByClassIdAndRoomIdAndRemovedAtIsNull(sourceMap.getClassId(), destinationRoomId)
                            .isPresent()) {
                        continue;
                    }

                    RoomMap clonedMap = cloneRoomMap(sourceMap, sourceLocations, destinationRoomId);
                    RoomMap savedClone = roomMapRepository.save(clonedMap);

                    recordRoomMapHistoryUseCase.record(
                            savedClone.getId(),
                            RoomMapHistoryAction.MAP_REPLICATED.name(),
                            requestContextProvider.getRequestContext().userId(),
                            "Mapa replicado a partir do mapa " + sourceMap.getId()
                    );

                    replicatedRoomIds.add(destinationRoomId);
                } catch (Exception ex) {
                    log.warn("Failed to replicate room map to roomId={}", destinationRoomId, ex);
                }
            }

            recordSourceHistoryWhenNeeded(sourceMap, replicatedRoomIds);
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

    private void recordSourceHistoryWhenNeeded(RoomMap sourceMap, List<UUID> replicatedRoomIds) {
        if (replicatedRoomIds.isEmpty()) {
            return;
        }

        try {
            recordRoomMapHistoryUseCase.record(
                    sourceMap.getId(),
                    RoomMapHistoryAction.MAP_REPLICATED.name(),
                    requestContextProvider.getRequestContext().userId(),
                    "Mapa replicado para as salas " + replicatedRoomIds
            );
        } catch (Exception ex) {
            log.warn("Failed to record replication history for sourceRoomMapId={}", sourceMap.getId(), ex);
        }
    }
}
