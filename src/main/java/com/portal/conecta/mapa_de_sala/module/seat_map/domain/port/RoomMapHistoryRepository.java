package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomMapHistoryRepository extends JpaRepository<RoomMapHistory, UUID> {

    List<RoomMapHistory> findByRoomMapIdOrderByCreatedAtDesc(UUID roomMap);
}
