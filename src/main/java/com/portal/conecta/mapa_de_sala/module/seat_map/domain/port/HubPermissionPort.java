package com.portal.conecta.mapa_de_sala.module.seat_map.domain.port;

import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

import java.util.List;
import java.util.UUID;

/**
 * Porta de integração com o Hub para consulta das turmas acessíveis a um usuário.
 */
public interface HubPermissionPort {

    List<UUID> getAccessibleClassIds(UUID userId, TypeUser userType);
}
