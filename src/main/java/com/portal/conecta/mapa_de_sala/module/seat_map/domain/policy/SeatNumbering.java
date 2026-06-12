package com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy;

import java.util.Map;
import java.util.UUID;

//maps only STUDENT positions
public final class SeatNumbering {
    private final Map<UUID, Integer> seatNumberByPositionId;

    public SeatNumbering(Map<UUID, Integer> seatNumberByPositionId) {
        this.seatNumberByPositionId = Map.copyOf(seatNumberByPositionId);
    }

    //returns the seat position or null if nor is STUDENT.
    public Integer seatNumberOf(UUID layoutPositionId){
        return seatNumberByPositionId.get(layoutPositionId);
    }

    public int totalSeats(){
        return seatNumberByPositionId.size();
    }
}
