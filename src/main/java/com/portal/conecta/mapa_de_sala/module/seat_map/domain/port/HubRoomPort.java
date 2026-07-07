package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import java.util.UUID;

/**
 * Porta de integração com o Hub para validação de salas.
 */
public interface HubRoomPort {

    boolean existsById(UUID roomId);
}
