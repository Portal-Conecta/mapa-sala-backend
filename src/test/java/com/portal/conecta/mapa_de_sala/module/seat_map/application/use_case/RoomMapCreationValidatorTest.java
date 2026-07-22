package com.portal.conecta.mapa_de_sala.module.seat_map.application.use_case;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ConflictException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.exception.ResourceNotFoundException;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.RoomMap;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.policy.RoomMapCreationValidator;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubRoomPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.RoomMapRepository;
import com.portal.conecta.mapa_de_sala.shared.context.ClassRole;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;


class RoomMapCreationValidatorTest {

    private HubClassPort hubClassPort;
    private HubRoomPort hubRoomPort;
    private RoomMapRepository roomMapRepository;
    private RoomMapCreationValidator validator;

    private UUID classId;
    private UUID roomId;

    @BeforeEach
    void setUp() {
        hubClassPort = mock(HubClassPort.class);
        hubRoomPort = mock(HubRoomPort.class);
        roomMapRepository = mock(RoomMapRepository.class);
        validator = new RoomMapCreationValidator(hubClassPort, hubRoomPort, roomMapRepository);

        classId = UUID.randomUUID();
        roomId = UUID.randomUUID();

        when(hubClassPort.existsById(classId)).thenReturn(true);
        when(hubRoomPort.existsById(roomId)).thenReturn(true);
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId))
                .thenReturn(Optional.empty());
    }

    private RequestContext contextWith(TypeUser userType, List<ContextClass> classes) {
        return new RequestContext(UUID.randomUUID(), userType, classes);
    }

    @Test
    void shouldAllowAdminToCreateMapWithoutClassLink() {
        RequestContext context = contextWith(TypeUser.ADMIN, List.of());

        assertThatCode(() -> validator.validatePreConditions(classId, roomId, context))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowSenaiToCreateMapWithoutClassLink() {
        RequestContext context = contextWith(TypeUser.SENAI, List.of());

        assertThatCode(() -> validator.validatePreConditions(classId, roomId, context))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowWegToCreateMapWithoutClassLink() {
        RequestContext context = contextWith(TypeUser.WEG, List.of());

        assertThatCode(() -> validator.validatePreConditions(classId, roomId, context))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowTeacherLinkedToClass() {
        RequestContext context = contextWith(
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        );

        assertThatCode(() -> validator.validatePreConditions(classId, roomId, context))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenTeacherNotLinkedToClass() {
        RequestContext context = contextWith(TypeUser.TEACHER, List.of());

        assertThatThrownBy(() -> validator.validatePreConditions(classId, roomId, context))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Docente não vinculado à turma.");
    }

    @Test
    void shouldThrowWhenTeacherLinkedToDifferentClass() {
        RequestContext context = contextWith(
                TypeUser.TEACHER,
                List.of(new ContextClass(UUID.randomUUID(), ClassRole.TEACHER))
        );

        assertThatThrownBy(() -> validator.validatePreConditions(classId, roomId, context))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldThrowResourceNotFoundWhenClassDoesNotExist() {
        when(hubClassPort.existsById(classId)).thenReturn(false);
        RequestContext context = contextWith(TypeUser.ADMIN, List.of());

        assertThatThrownBy(() -> validator.validatePreConditions(classId, roomId, context))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(roomMapRepository);
    }

    @Test
    void shouldThrowResourceNotFoundWhenRoomDoesNotExist() {
        when(hubRoomPort.existsById(roomId)).thenReturn(false);
        RequestContext context = contextWith(TypeUser.ADMIN, List.of());

        assertThatThrownBy(() -> validator.validatePreConditions(classId, roomId, context))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowConflictWhenActiveMapAlreadyExistsEvenForAdmin() {
        RoomMap existingMap = mock(RoomMap.class);
        when(roomMapRepository.findByClassIdAndRoomIdAndRemovedAtIsNull(classId, roomId))
                .thenReturn(Optional.of(existingMap));

        RequestContext context = contextWith(TypeUser.ADMIN, List.of());

        assertThatThrownBy(() -> validator.validatePreConditions(classId, roomId, context))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Já existe um mapa ativo para esta turma e sala.");
    }
}
