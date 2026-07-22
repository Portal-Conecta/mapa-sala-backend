package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.shared.context.ClassRole;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

class RoomLayoutAuthorizationServiceTest {

    private RoomLayoutAuthorizationService authorizationService;
    private HubRoomPort hubRoomPort;

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID classId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        hubRoomPort = mock(HubRoomPort.class);
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        authorizationService = new RoomLayoutAuthorizationService(hubRoomPort);
    }

    @Test
    void mustAllowGlobalProfile() {
        var user = new RequestContext(userId, TypeUser.SENAI, List.of());

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();
    }

    @Test
    void mustAllowTeacherWithAnyClassLinked() {
        var user = new RequestContext(userId, TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();
    }

    @Test
    void mustDenyTeacherWithoutClassInToken() {
        var user = new RequestContext(userId, TypeUser.TEACHER, List.of());

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void mustDenyNullUser() {
        assertThatThrownBy(() -> authorizationService.checkReadAccess(null, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void mustDenyWhenRoomDoesNotExistInHub() {
        var user = new RequestContext(userId, TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void mustNotCheckRoomExistenceForGlobalProfile() {
        var user = new RequestContext(userId, TypeUser.SENAI, List.of());
        when(hubRoomPort.existsById(roomId)).thenReturn(false);

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();
    }
}
