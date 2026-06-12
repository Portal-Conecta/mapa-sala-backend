package com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.assembler.result.AllocationInput;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.SeatNumbering;
import com.portal.conecta.mapa_de_sala.module.seat_map.presentation.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RoomMapViewAssembler {

    private static final Comparator<LayoutPosition> READING_ORDER =
            Comparator.comparingInt((LayoutPosition::getPositionY))
                    .thenComparingInt(LayoutPosition::getPositionX);

    public RoomMapViewResponse assemble(
            boolean suggested,
            RoomMapResponse mapResponse,
            int rows,
            int columns,
            List<LayoutPosition> positions,
            SeatNumbering numbering,
            List<AllocationInput> allocations,
            List<UnassignedStudentResponse> unassigned
    ){
        return new RoomMapViewResponse(
                suggested,
                mapResponse,
                buidGrid(rows, columns, positions, numbering),
                buildAllocations(allocations, numbering),
                unassigned
        );
    }

    private RoomMapGridResponse buidGrid(
            int rows,
            int columns,
            List<LayoutPosition> positions,
            SeatNumbering numbering
    ){
        List<RoomMapGridPositionResponse> positionResponses = positions.stream()
                .sorted(READING_ORDER)
                .map(p -> new RoomMapGridPositionResponse(
                        p.getId(),
                        numbering.seatNumberOf(p.getId()),
                        p.getPositionX(),
                        p.getPositionY(),
                        p.getType()
                )).toList();

        return new RoomMapGridResponse(rows, columns, numbering.totalSeats(),  positionResponses);
    }

    private List<RoomMapAllocationResponse> buildAllocations(
            List<AllocationInput> allocations,
            SeatNumbering numbering){

        return allocations.stream()
                .map(a -> new RoomMapAllocationResponse(
                        a.studentId(),
                        a.studentName(),
                        numbering.seatNumberOf(a.layoutPositionId()),
                        a.layoutPositionId()))
                .sorted(Comparator.comparing(RoomMapAllocationResponse::seatNumber))
                .toList();
    }

}
