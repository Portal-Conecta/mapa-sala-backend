package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class RoomMapViewAuthorizationService {

    private final HubClassPort hubClassPort;
    private final HubPermissionPort hubPermissionPort;

    public RoomMapViewAuthorizationService(
            HubClassPort hubClassPort,
            HubPermissionPort hubPermissionPort
    ) {
        this.hubClassPort = hubClassPort;
        this.hubPermissionPort = hubPermissionPort;
    }

    public void ensureCanViewClass(RequestContext context, UUID classId){
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(classId, "classId must not be null");

        if (canViewClass(context, classId)) {
            return;
        }
        throw new AccessDeniedException(
                    "The user does not have permission to view this class's map."
        );
    }

    private boolean canViewClass(RequestContext context, UUID classId){
        UUID userId = context.userId();
        TypeUser userType = context.userType();

        return switch (userType){
            case STUDENT, REPRESENTATIVE ->
                classId.equals(hubClassPort.getClassIdForUser(userId));
            case TEACHER ->
                hubPermissionPort.getAccessibleClassIds(userId, userType).contains((classId));
            case SENAI, WEG, ADMIN ->
                true;
        };
    }
}
