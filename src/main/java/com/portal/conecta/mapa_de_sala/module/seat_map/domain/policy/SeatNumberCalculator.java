package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.enums.LayoutPositionType;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.LayoutPosition;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SeatNumberCalculator {

    private static final Comparator<LayoutPosition> READING_ORDER =
            Comparator.comparingInt(LayoutPosition::getPositionY)
                    .thenComparing(LayoutPosition::getPositionX);

    public SeatNumbering calculate(List<LayoutPosition> positions){
        Objects.requireNonNull(positions, "positions cannot be null");

        Map<UUID, Integer> seatNumberByPositionId = new LinkedHashMap<>();
        int nextSeatNumber = 1;

        List<LayoutPosition> ordered = positions.stream()
                .sorted(READING_ORDER)
                .toList();

        //You can only advance if the position is STUDENT.
        for (LayoutPosition position : ordered){
            if (position.getType() == LayoutPositionType.STUDENT){
                seatNumberByPositionId.put(position.getId(), nextSeatNumber);
                nextSeatNumber++;
            }
        }

        return new SeatNumbering(seatNumberByPositionId);
    }
}
