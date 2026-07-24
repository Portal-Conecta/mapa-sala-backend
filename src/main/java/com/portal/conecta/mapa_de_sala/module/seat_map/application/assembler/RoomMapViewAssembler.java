package com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapAllocationResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapGridPositionResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapGridResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.RoomMapViewResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.UnassignedStudentResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.mapper.RoomMapMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class RoomMapViewAssembler {

    private static final Comparator<LayoutPosition> READING_ORDER =
            Comparator.comparingInt(LayoutPosition::getPositionY)
                    .thenComparingInt(LayoutPosition::getPositionX);

    private final RoomMapMapper roomMapMapper;

    public RoomMapViewAssembler(RoomMapMapper roomMapMapper) {
        this.roomMapMapper = roomMapMapper;
    }

    public RoomMapViewResponse assembleFromSavedMap(
            RoomMap roomMap,
            int rows,
            int columns,
            List<LayoutPosition> positions,
            SeatNumbering numbering,
            List<RoomMapLocation> locations,
            List<HubStudent> classStudents
    ) {
        Map<UUID, String> nameById = indexNames(classStudents);

        List<RoomMapAllocationResponse> allocations = locations.stream()
                .filter(location -> numbering.seatNumberOf(location.getLayoutPositionId()) != null)
                .filter(location -> nameById.containsKey(location.getStudentId()))
                .map(location -> new RoomMapAllocationResponse(
                        location.getStudentId(),
                        nameById.get(location.getStudentId()),
                        numbering.seatNumberOf(location.getLayoutPositionId()),
                        location.getLayoutPositionId()))
                .sorted(Comparator.comparing(RoomMapAllocationResponse::seatNumber))
                .toList();

        List<UnassignedStudentResponse> unassigned = buildUnassigned(classStudents, allocations);

        return new RoomMapViewResponse(
                false,
                roomMapMapper.toResponse(roomMap),
                buildGrid(rows, columns, positions, numbering),
                allocations,
                unassigned
        );
    }

    public RoomMapViewResponse assembleFromSuggestion(
            int rows,
            int columns,
            List<LayoutPosition> positions,
            SeatNumbering numbering,
            List<HubStudent> classStudents
    ) {
        List<LayoutPosition> studentSeats = positions.stream()
                .filter(p -> numbering.seatNumberOf(p.getId()) != null)
                .toList();

        List<RoomMapAllocationResponse> allocations = new ArrayList<>();

        for (int i = 0; i < Math.min(classStudents.size(), studentSeats.size()); i++) {
            HubStudent student = classStudents.get(i);
            LayoutPosition seat = studentSeats.get(i);
            allocations.add(new RoomMapAllocationResponse(
                    student.id(),
                    student.name(),
                    numbering.seatNumberOf(seat.getId()),
                    seat.getId()));
        }

        List<UnassignedStudentResponse> unassigned = buildUnassigned(classStudents, allocations);

        return new RoomMapViewResponse(
                true,
                null,
                buildGrid(rows, columns, positions, numbering),
                allocations,
                unassigned
        );
    }

    private RoomMapGridResponse buildGrid(
            int rows,
            int columns,
            List<LayoutPosition> positions,
            SeatNumbering numbering
    ) {
        List<RoomMapGridPositionResponse> positionResponses = positions.stream()
                .sorted(READING_ORDER)
                .map(p -> new RoomMapGridPositionResponse(
                        p.getId(),
                        numbering.seatNumberOf(p.getId()),
                        p.getPositionX(),
                        p.getPositionY(),
                        p.getType()))
                .toList();

        return new RoomMapGridResponse(rows, columns, numbering.totalSeats(), positionResponses);
    }

    private List<UnassignedStudentResponse> buildUnassigned(
            List<HubStudent> classStudents,
            List<RoomMapAllocationResponse> allocations
    ) {
        Set<UUID> allocatedIds = new HashSet<>();
        for (RoomMapAllocationResponse allocation : allocations) {
            allocatedIds.add(allocation.studentId());
        }

        return classStudents.stream()
                .filter(s -> !allocatedIds.contains(s.id()))
                .map(s -> new UnassignedStudentResponse(s.id(), s.name()))
                .toList();
    }

    private Map<UUID, String> indexNames(List<HubStudent> students) {
        Map<UUID, String> nameById = new HashMap<>();
        for (HubStudent student : students) {
            nameById.put(student.id(), student.name());
        }
        return nameById;
    }
}
