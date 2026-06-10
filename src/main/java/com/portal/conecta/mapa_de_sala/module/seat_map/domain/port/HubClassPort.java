package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import java.util.UUID;

/**
 * Porta de integração com o Hub para consulta da turma própria de um usuário.
 */
public interface HubClassPort {

    UUID getClassIdForUser(UUID userId);
}
