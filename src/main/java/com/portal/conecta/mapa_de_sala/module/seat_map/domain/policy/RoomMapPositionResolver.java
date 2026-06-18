package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.application.command.CreateRoomMapInitialAllocationCommand;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.BadRequestException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RoomMapPositionResolver {

    public UUID resolvePositionId(
            CreateRoomMapInitialAllocationCommand allocation,
            List<LayoutPosition> positions,
            SeatNumbering numbering) {

        if (allocation.layoutPositionId() != null) {
            return allocation.layoutPositionId();
        }

        if (allocation.seatNumber() != null) {
            return positions.stream()
                    .filter(pos -> allocation.seatNumber().equals(numbering.seatNumberOf(pos.getId())))
                    .map(LayoutPosition::getId)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("O número do assento (" + allocation.seatNumber() + ") não existe neste template."));
        }

        throw new BadRequestException("É obrigatório informar seatNumber ou layoutPositionId na alocação.");
    }
}