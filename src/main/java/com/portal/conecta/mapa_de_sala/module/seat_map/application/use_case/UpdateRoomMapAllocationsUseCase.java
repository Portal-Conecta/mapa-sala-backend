package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.RoomMapViewAssembler;
import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.UpdateRoomMapAllocationsCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.RoomMapHistoryAction;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumberCalculator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.*;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.request.AllocationEntryRequest;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateRoomMapAllocationsUseCase {

    private final RoomMapRepository roomMapRepository;
    private final RoomMapLocationRepository roomMapLocationRepository;
    private final LayoutPositionRepository layoutPositionRepository;
    private final RoomMapHistoryRepository roomMapHistoryRepository;
    private final HubClassPort hubClassPort;
    private final SeatNumberCalculator seatNumberCalculator;
    private final RoomMapViewAssembler assembler;
    private final RequestContextProvider requestContextProvider;

    @Transactional
    public RoomMapViewResponse execute(UpdateRoomMapAllocationsCommand command) {
        UUID roomMapId = command.roomMapId();
        List<AllocationEntryRequest> entries = command.data().allocations();

        RoomMap roomMap = roomMapRepository.findById(roomMapId)
                .filter(RoomMap::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Mapa de sala", roomMapId));

        RequestContext context = requestContextProvider.getRequestContext();
        ensureCanEdit(context, roomMap);

        List<LayoutPosition> templatePositions = layoutPositionRepository
                .findByLayoutTemplateIdOrderByPositionYAscPositionXAsc(roomMap.getLayoutTemplateId());

        Map<UUID, LayoutPosition> positionById = templatePositions.stream()
                .collect(Collectors.toMap(LayoutPosition::getId, Function.identity()));

        SeatNumbering numbering = seatNumberCalculator.calculate(templatePositions);

        List<HubStudent> classStudents = hubClassPort.findStudentsByClassId(roomMap.getClassId());
        Set<UUID> classStudentIds = classStudents.stream()
                .map(HubStudent::id)
                .collect(Collectors.toSet());

        validateAllocations(entries, positionById, classStudentIds);

        roomMapLocationRepository.deleteByRoomMapId(roomMapId);
        roomMapLocationRepository.flush();

        List<RoomMapLocation> newLocations = new ArrayList<>();
        for (AllocationEntryRequest entry : entries) {
            var location = new RoomMapLocation();
            location.setRoomMap(roomMap);
            location.setStudentId(entry.studentId());
            location.setLayoutPosition(positionById.get(entry.layoutPositionId()));
            newLocations.add(location);
        }

        roomMapLocationRepository.saveAll(newLocations);
        roomMapLocationRepository.flush();

        var history = new RoomMapHistory();
        history.setRoomMap(roomMap);
        history.setUserId(context.userId());
        history.setAction(RoomMapHistoryAction.MAP_UPDATED);
        history.setDetails("Alocações do mapa atualizadas: %d alunos alocados.".formatted(entries.size()));
        roomMapHistoryRepository.save(history);

        var template = roomMap.getLayoutTemplate();
        return assembler.assembleFromSavedMap(
                roomMap,
                template.getDimensionY(),
                template.getDimensionX(),
                templatePositions,
                numbering,
                newLocations,
                classStudents
        );
    }

    private void ensureCanEdit(RequestContext context, RoomMap roomMap) {
        if (context.userType() == TypeUser.ADMIN) {
            return;
        }

        if (context.userType() == TypeUser.TEACHER) {
            boolean linked = context.classes().stream()
                    .anyMatch(c -> c.classId().equals(roomMap.getClassId()));
            if (linked) {
                return;
            }
        }

        throw new AccessDeniedException("Usuário não autorizado para editar as alocações deste mapa de sala");
    }

    private void validateAllocations(
            List<AllocationEntryRequest> entries,
            Map<UUID, LayoutPosition> positionById,
            Set<UUID> classStudentIds
    ) {
        Set<UUID> seenStudents = new HashSet<>();
        Set<UUID> seenPositions = new HashSet<>();

        for (AllocationEntryRequest entry : entries) {
            if (!seenStudents.add(entry.studentId())) {
                throw new BadRequestException("studentId duplicado na lista: " + entry.studentId());
            }

            if (!seenPositions.add(entry.layoutPositionId())) {
                throw new BadRequestException("layoutPositionId duplicado na lista: " + entry.layoutPositionId());
            }

            LayoutPosition position = positionById.get(entry.layoutPositionId());
            if (position == null) {
                throw new BadRequestException(
                        "layoutPositionId não pertence ao template do mapa: " + entry.layoutPositionId());
            }

            if (position.getType() != LayoutPositionType.STUDENT) {
                throw new BadRequestException(
                        "layoutPositionId não é do tipo STUDENT: " + entry.layoutPositionId()
                                + " (tipo atual: " + position.getType() + ")");
            }

            if (!classStudentIds.contains(entry.studentId())) {
                throw new BadRequestException("studentId não pertence à turma do mapa: " + entry.studentId());
            }
        }

        if (!seenStudents.equals(classStudentIds)) {
            Set<UUID> missing = new HashSet<>(classStudentIds);
            missing.removeAll(seenStudents);
            throw new BadRequestException(
                    "Todos os alunos da turma devem estar alocados. Alunos ausentes: " + missing);
        }
    }
}