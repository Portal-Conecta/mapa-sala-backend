package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomLayoutRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapHistoryRepository;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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

    private final RoomMapRepository roomMapRepository;
    private final RoomLayoutRepository roomLayoutRepository;
    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final PlatformTransactionManager transactionManager;

    public void replicateAfterCommit(RoomMap sourceMap, UUID userId) {
        UUID sourceMapId = sourceMap.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    replicateBestEffort(sourceMapId, userId);
                }
            });
            return;
        }

        replicateBestEffort(sourceMapId, userId);
    }

    void replicateBestEffort(UUID sourceMapId, UUID userId) {
        try {
            ReplicationPlan plan = loadPlan(sourceMapId);
            List<UUID> replicatedRoomIds = new ArrayList<>();

            for (UUID targetRoomId : plan.targetRoomIds()) {
                try {
                    if (createClone(sourceMapId, userId, targetRoomId)) {
                        replicatedRoomIds.add(targetRoomId);
                    }
                } catch (Exception ex) {
                    log.warn("Falha ao replicar mapa de sala para roomId={}: {}", targetRoomId, ex.getMessage(), ex);
                }
            }

            if (!replicatedRoomIds.isEmpty()) {
                recordSourceReplication(sourceMapId, userId, replicatedRoomIds);
            }
        } catch (Exception ex) {
            log.warn("Falha ao preparar replicacao do mapa de sala sourceRoomMapId={}: {}", sourceMapId, ex.getMessage(), ex);
        }
    }

    private ReplicationPlan loadPlan(UUID sourceMapId) {
        return requiresNew().execute(status -> {
            RoomMap sourceMap = roomMapRepository.findById(sourceMapId)
                    .orElseThrow(() -> new IllegalStateException("Mapa origem nao encontrado."));

            List<UUID> targetRoomIds = roomLayoutRepository.findByLayoutTemplateId(sourceMap.getLayoutTemplateId()).stream()
                    .map(RoomLayout::getRoomId)
                    .filter(roomId -> !roomId.equals(sourceMap.getRoomId()))
                    .filter(roomId -> roomMapRepository
                            .findByClassIdAndRoomIdAndRemovedAtIsNull(sourceMap.getClassId(), roomId)
                            .isEmpty())
                    .toList();

            return new ReplicationPlan(targetRoomIds);
        });
    }

    private boolean createClone(UUID sourceMapId, UUID userId, UUID targetRoomId) {
        Boolean created = requiresNew().execute(status -> {
            RoomMap sourceMap = roomMapRepository.findById(sourceMapId)
                    .orElseThrow(() -> new IllegalStateException("Mapa origem nao encontrado."));

            if (roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(sourceMap.getClassId(), targetRoomId).isPresent()) {
                return false;
            }

            roomMapRepository.save(RoomMap.replicateFrom(sourceMap, targetRoomId, userId));
            return true;
        });
        return Boolean.TRUE.equals(created);
    }

    private void recordSourceReplication(UUID sourceMapId, UUID userId, List<UUID> replicatedRoomIds) {
        requiresNew().executeWithoutResult(status -> {
            roomMapHistoryRepository.save(RoomMapHistory.create(
                    roomMapRepository.getReferenceById(sourceMapId),
                    userId,
                    RoomMapHistoryAction.MAP_REPLICATED,
                    "Mapa replicado para " + replicatedRoomIds.size() + " sala(s): " + replicatedRoomIds + "."
            ));
        });
    }

    private TransactionTemplate requiresNew() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    private record ReplicationPlan(List<UUID> targetRoomIds) {
    }
}
