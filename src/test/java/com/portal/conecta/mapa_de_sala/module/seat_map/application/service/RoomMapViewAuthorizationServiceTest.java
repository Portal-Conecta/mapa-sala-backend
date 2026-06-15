package com.portal.conecta.mapa_de_sala.module.seat_map.application.service;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubClassPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubPermissionPort;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RoomMapViewAuthorizationServiceTest {

    @Mock private HubClassPort hubClassPort;
    @Mock private HubPermissionPort hubPermissionPort;

    @InjectMocks
    private RoomMapViewAuthorizationService service;

    private RequestContext context(UUID userId, TypeUser type) {
        // Ajuste para o construtor real do RequestContext do seat_map.
        return new RequestContext(userId, type, List.of());
    }

    // --- STUDENT / REPRESENTATIVE: só a própria turma ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE"})
    @DisplayName("STUDENT/REPRESENTATIVE acessa a própria turma")
    void allowsStudentOnOwnClass(TypeUser type) {
        UUID userId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(turmaId);

        assertThatCode(() -> service.ensureCanViewClass(context(userId, type), turmaId))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"STUDENT", "REPRESENTATIVE"})
    @DisplayName("STUDENT/REPRESENTATIVE é barrado em turma alheia")
    void deniesStudentOnOtherClass(TypeUser type) {
        UUID userId = UUID.randomUUID();
        UUID ownClass = UUID.randomUUID();
        UUID otherClass = UUID.randomUUID();
        when(hubClassPort.getClassIdForUser(userId)).thenReturn(ownClass);

        assertThatThrownBy(() -> service.ensureCanViewClass(context(userId, type), otherClass))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- TEACHER: turmas acessíveis ---

    @Test
    @DisplayName("TEACHER acessa turma que está na lista de acessíveis")
    void allowsTeacherOnAccessibleClass() {
        UUID userId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        when(hubPermissionPort.getAccessibleClassIds(userId, TypeUser.TEACHER))
                .thenReturn(List.of(UUID.randomUUID(), turmaId));

        assertThatCode(() -> service.ensureCanViewClass(context(userId, TypeUser.TEACHER), turmaId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TEACHER é barrado em turma fora da lista de acessíveis")
    void deniesTeacherOnInaccessibleClass() {
        UUID userId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        when(hubPermissionPort.getAccessibleClassIds(userId, TypeUser.TEACHER))
                .thenReturn(List.of(UUID.randomUUID()));

        assertThatThrownBy(() -> service.ensureCanViewClass(context(userId, TypeUser.TEACHER), turmaId))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- Perfis globais: liberados sem consultar o Hub ---

    @ParameterizedTest
    @EnumSource(value = TypeUser.class, names = {"SENAI", "WEG", "ADMIN"})
    @DisplayName("SENAI/WEG/ADMIN acessam qualquer turma sem consultar o Hub")
    void allowsGlobalProfiles(TypeUser type) {
        UUID userId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();

        assertThatCode(() -> service.ensureCanViewClass(context(userId, type), turmaId))
                .doesNotThrowAnyException();

        verifyNoInteractions(hubClassPort, hubPermissionPort);
    }
}
