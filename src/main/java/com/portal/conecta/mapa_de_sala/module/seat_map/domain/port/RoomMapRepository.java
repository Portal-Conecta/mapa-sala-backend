package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoomMapRepository extends JpaRepository<RoomMap, UUID> {

    Optional<RoomMap> findByClassIdAndRoomIdAndRemovedAtIsNull(UUID classId, UUID roomId);

    List<RoomMap> findByClassIdInAndRemovedAtIsNull(Collection<UUID> classIds);

    List<RoomMap> findByClassIdAndRemovedAtIsNull(UUID classId);

    Page<RoomMap> findByClassIdInAndRemovedAtIsNull(List<UUID> classIds, Pageable pageable);

    boolean existsByClassIdInAndRoomIdAndRemovedAtIsNull(Collection<UUID> classIds, UUID roomId);

    Page<RoomMap> findByClassIdInAndRoomIdAndRemovedAtIsNull(List<UUID> classIds, UUID roomId, Pageable pageable);

    Page<RoomMap> findAllByRemovedAtIsNull(Pageable pageable);

    Page<RoomMap> findAllByRemovedAtIsNullAndRoomId(UUID roomId, Pageable pageable);
}
