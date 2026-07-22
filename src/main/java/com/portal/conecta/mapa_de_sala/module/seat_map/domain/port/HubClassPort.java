package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;

import java.util.List;
import java.util.UUID;

/**
 * Porta de integração com o Hub para consulta de vínculo de matrícula do usuário.
 */
public interface HubClassPort {

    boolean belongsToClass(UUID userId, UUID classId);

    List<UUID> getClassIdsForUser(UUID userId);

    boolean existsById(UUID classId);

    List<HubStudent> findStudentsByClassId(UUID classId);
}