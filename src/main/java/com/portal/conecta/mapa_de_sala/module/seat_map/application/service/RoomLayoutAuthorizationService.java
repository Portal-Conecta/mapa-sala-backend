package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomLayoutAuthorizationService {

    private final HubRoomPort hubRoomPort;

    public RoomLayoutAuthorizationService(HubRoomPort hubRoomPort) {
        this.hubRoomPort = hubRoomPort;
    }

    public void checkReadAccess(RequestContext user, UUID roomId) {
        if (isGlobalProfile(user.userType())) {
            return;
        }

        if (!hubRoomPort.isUserLinkedToRoom(user.userId(), roomId)) {
            throw new AccessDeniedException("Acesso negado à sala solicitada");
        }
    }

    private boolean isGlobalProfile(TypeUser type) {
        return type == TypeUser.SENAI
            || type == TypeUser.WEG
            || type == TypeUser.ADMIN;
    }
}