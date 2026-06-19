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
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MoveStudentUseCase {

    private final RoomMapRepository roomMapRepository;
    private final RoomMapLocationRepository roomMapLocationRepository;
    private final LayoutPositionRepository layoutPositionRepository;
    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final RequestContextProvider requestContextProvider;

    public void execute(MoveStudentCommand command) {
        UUID roomMapId = command.roomMapId();
        UUID studentId = command.data().studentId();
        UUID targetLayoutPositionId = command.data().targetLayoutPositionId();

        MoveConflictStrategy strategy = command.data().onConflict() != null
                ? command.data().onConflict()
                : MoveConflictStrategy.DISPLACE;

        RoomMap roomMap = roomMapRepository.findById(roomMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapa de sala", roomMapId));

        if (!roomMap.isActive()) {
            throw new RoomMapAlreadyArchivedException(roomMapId);
        }

        RequestContext user = requestContextProvider.getRequestContext();
        if (!isAuthorized(user, roomMap)) {
            throw new AccessDeniedException("Usuário não autorizado para editar este mapa de sala");
        }

        LayoutPosition targetPosition = layoutPositionRepository.findById(targetLayoutPositionId)
                .orElseThrow(() -> new ResourceNotFoundException("Posição de layout", targetLayoutPositionId));

        if (targetPosition.getType() != LayoutPositionType.STUDENT) {
            throw new InvalidLayoutPositionTypeException(targetLayoutPositionId, targetPosition.getType());
        }

        RoomMapLocation studentLocation = roomMapLocationRepository
                .findByRoomMapIdAndStudentId(roomMapId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Alocação do aprendiz", studentId));

        Optional<RoomMapLocation> occupantLocation = roomMapLocationRepository
                .findByRoomMapIdAndLayoutPositionId(roomMapId, targetLayoutPositionId);

        if (occupantLocation.isPresent()) {
            if (strategy == MoveConflictStrategy.SWAP) {
                executeSwap(studentLocation, occupantLocation.get(), targetPosition, roomMap, user);
            } else {
                executeDisplace(studentLocation, occupantLocation.get(), targetPosition, roomMap, user);
            }
        } else {
            executeMove(studentLocation, targetPosition, roomMap, user);
        }
    }

    private void executeMove(
            RoomMapLocation studentLocation,
            LayoutPosition targetPosition,
            RoomMap roomMap,
            RequestContext user
    ) {
        studentLocation.setLayoutPosition(targetPosition);
        roomMapLocationRepository.save(studentLocation);

        saveHistory(roomMap, user.userId(), RoomMapHistoryAction.STUDENT_MOVED);
    }

    private void executeDisplace(
            RoomMapLocation studentLocation,
            RoomMapLocation occupantLocation,
            LayoutPosition targetPosition,
            RoomMap roomMap,
            RequestContext user
    ) {
        roomMapLocationRepository.delete(occupantLocation);
        studentLocation.setLayoutPosition(targetPosition);
        roomMapLocationRepository.save(studentLocation);

        saveHistory(roomMap, user.userId(), RoomMapHistoryAction.STUDENT_UNASSIGNED);
        saveHistory(roomMap, user.userId(), RoomMapHistoryAction.STUDENT_MOVED);
    }

    private void executeSwap(
            RoomMapLocation studentLocation,
            RoomMapLocation occupantLocation,
            LayoutPosition targetPosition,
            RoomMap roomMap,
            RequestContext user
    ) {
        LayoutPosition originalPosition = studentLocation.getLayoutPosition();

        roomMapLocationRepository.delete(studentLocation);
        roomMapLocationRepository.delete(occupantLocation);
        roomMapLocationRepository.flush();

        RoomMapLocation movedStudent = new RoomMapLocation();
        movedStudent.setRoomMap(roomMap);
        movedStudent.setStudentId(studentLocation.getStudentId());
        movedStudent.setLayoutPosition(targetPosition);

        RoomMapLocation movedOccupant = new RoomMapLocation();
        movedOccupant.setRoomMap(roomMap);
        movedOccupant.setStudentId(occupantLocation.getStudentId());
        movedOccupant.setLayoutPosition(originalPosition);

        roomMapLocationRepository.save(movedStudent);
        roomMapLocationRepository.save(movedOccupant);

        saveHistory(roomMap, user.userId(), RoomMapHistoryAction.STUDENTS_SWAPPED);
    }

    private void saveHistory(RoomMap roomMap, UUID userId, RoomMapHistoryAction action) {
        var history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(userId);
        history.setAction(action);
        roomMapHistoryRepository.save(history);
    }

    private boolean isAuthorized(RequestContext user, RoomMap roomMap) {
        if (user.userType() == TypeUser.TEACHER) {
            return user.classes()
                    .stream()
                    .anyMatch(c -> c.classId().equals(roomMap.getClassId()));
        }
        return false;
    }
}