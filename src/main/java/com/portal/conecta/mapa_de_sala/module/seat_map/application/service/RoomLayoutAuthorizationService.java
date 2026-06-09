package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.shared.security.AuthenticatedUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomLayoutAuthorizationService {

    private final HubRoomPort hubRoomPort;

    public RoomLayoutAuthorizationService(HubRoomPort hubRoomPort) {
        this.hubRoomPort = hubRoomPort;
    }

    public void checkReadAccess(AuthenticatedUser user, UUID roomId) {
        if (user.profile().hasGlobalRoomAccess()) {
            return;
        }

        if (!hubRoomPort.isUserLinkedToRoom(user.userId(), roomId)) {
            throw new AccessDeniedException("Acesso negado à sala solicitada");
        }
    }
}
