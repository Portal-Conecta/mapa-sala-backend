package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapLocation;

import java.util.List;
import java.util.UUID;

public interface RoomMapLocationRepository extends JpaRepository<RoomMapLocation, UUID> {

    List<RoomMapLocation> findByRoomMapId(UUID roomMapId);

    boolean existsByRoomMapIdAndStudentId(UUID roomMapId, UUID studentId);

    boolean existsByRoomMapIdAndLayoutPositionId(UUID roomMapId, UUID layoutPositionId);

    void deleteByRoomMapId(UUID roomMapId);
}
