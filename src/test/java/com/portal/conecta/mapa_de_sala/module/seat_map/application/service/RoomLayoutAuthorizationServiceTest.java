package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.ClassRole;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomLayoutAuthorizationServiceTest {

    @Mock
    private RoomMapRepository roomMapRepository;

    private RoomLayoutAuthorizationService authorizationService;

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID classId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        authorizationService = new RoomLayoutAuthorizationService(roomMapRepository);
    }

    @Test
    void mustAllowGlobalProfileWithoutConsultingBank() {
        var user = new RequestContext(userId, TypeUser.SENAI, List.of());

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();

        verifyNoInteractions(roomMapRepository);
    }

    @Test
    void mustAllowTeacherWithClassLinkedtoRoom() {
        var user = new RequestContext(userId, TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));
        when(roomMapRepository.existsByClassIdInAndRoomIdAndRemovedAtIsNull(List.of(classId), roomId))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.checkReadAccess(user, roomId))
                .doesNotThrowAnyException();
    }

    @Test
    void mustDenyTeacherNoClassLinkedRoom() {
        var user = new RequestContext(userId, TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER)));
        when(roomMapRepository.existsByClassIdInAndRoomIdAndRemovedAtIsNull(List.of(classId), roomId))
                .thenReturn(false);

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void mustDenyProfessorWithoutNoTurmaNoToken() {
        var user = new RequestContext(userId, TypeUser.TEACHER, List.of());

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(roomMapRepository);
    }
}
