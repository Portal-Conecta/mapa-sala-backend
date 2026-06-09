package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.shared.security.AuthenticatedUser;
import com.portal.conecta.mapa_de_sala.shared.security.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomLayoutAuthorizationServiceTest {

    @Mock
    private HubRoomPort hubRoomPort;

    private RoomLayoutAuthorizationService authorizationService;

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        authorizationService = new RoomLayoutAuthorizationService(hubRoomPort);
    }

    @Test
    void checkReadAccess_shouldAllowGlobalProfilesWithoutHubCheck() {
        var user = new AuthenticatedUser(userId, UserProfile.PERFIL_SENAI);

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();

        verifyNoInteractions(hubRoomPort);
    }

    @Test
    void checkReadAccess_shouldAllowLinkedAprendiz() {
        var user = new AuthenticatedUser(userId, UserProfile.APRENDIZ);
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(true);

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();
    }

    @Test
    void checkReadAccess_shouldDenyUnlinkedDocente() {
        var user = new AuthenticatedUser(userId, UserProfile.DOCENTE);
        when(hubRoomPort.isUserLinkedToRoom(userId, roomId)).thenReturn(false);

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }
}
