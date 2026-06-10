package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de integração com o Hub para consulta de usuários (INT01).
 */
public interface HubUserPort {

    boolean existsById(UUID userId);

    Optional<HubUser> findById(UUID userId);
}
