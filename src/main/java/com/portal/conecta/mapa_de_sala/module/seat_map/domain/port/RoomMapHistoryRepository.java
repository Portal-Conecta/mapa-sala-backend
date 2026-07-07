package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMapHistory;

import java.util.UUID;

public interface RoomMapHistoryRepository extends JpaRepository<RoomMapHistory, UUID> {

    Page<RoomMapHistory> findByRoomMapIdOrderByCreatedAtDesc(UUID roomMap, Pageable pageable);
}
