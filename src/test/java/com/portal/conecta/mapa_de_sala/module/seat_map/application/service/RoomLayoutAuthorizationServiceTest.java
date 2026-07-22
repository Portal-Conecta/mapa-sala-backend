package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

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
import org.springframework.security.access.AccessDeniedException;

class RoomLayoutAuthorizationServiceTest {

    private RoomLayoutAuthorizationService authorizationService;

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID classId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        authorizationService = new RoomLayoutAuthorizationService();
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
    void mustDenyProfessorWithoutNoTurmaNoToken() {
        var user = new RequestContext(userId, TypeUser.TEACHER, List.of());

        assertThatThrownBy(() -> authorizationService.checkReadAccess(user, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void mustDenyNullUser() {
        assertThatThrownBy(() -> authorizationService.checkReadAccess(null, roomId))
                .isInstanceOf(AccessDeniedException.class);
    }
}
