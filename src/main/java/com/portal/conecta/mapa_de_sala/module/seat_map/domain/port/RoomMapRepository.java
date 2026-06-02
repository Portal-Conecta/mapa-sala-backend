package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoomMapRepository extends JpaRepository<RoomMap, UUID> {

    Optional<RoomMap> findByClassIdAndRoomIdAndRemovedAtIsNull(UUID classId, UUID roomId);

    List<RoomMap> findByClassIdInAndRemovedAtIsNull(Collection<UUID> classIds);

    List<RoomMap> findByClassIdAndRemovedAtIsNull(UUID classId);
}
