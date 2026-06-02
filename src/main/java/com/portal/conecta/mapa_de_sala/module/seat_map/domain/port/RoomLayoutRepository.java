package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomLayout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomLayoutRepository extends JpaRepository<RoomLayout, UUID> {

    Optional<RoomLayout> findByRoomId(UUID roomId);

    List<RoomLayout> findByLayoutTemplateId(UUID layoutTemplateId);
}
