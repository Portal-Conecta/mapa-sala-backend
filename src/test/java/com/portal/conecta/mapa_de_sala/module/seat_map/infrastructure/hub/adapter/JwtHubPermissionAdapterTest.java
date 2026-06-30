package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import com.portal.conecta.mapa_de_sala.shared.context.ClassRole;
import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContextProvider;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtHubPermissionAdapterTest {

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private JwtHubPermissionAdapter adapter;

    @Test
    void retornaClassIdsOndeRoleETeacher() {
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RequestContext context = new RequestContext(userId, TypeUser.TEACHER, List.of(
                new ContextClass(classId, ClassRole.TEACHER),
                new ContextClass(UUID.randomUUID(), ClassRole.STUDENT)
        ));
        when(contextProvider.getRequestContext()).thenReturn(context);

        List<UUID> result = adapter.getAccessibleClassIds(userId, TypeUser.TEACHER);

        assertThat(result).containsExactly(classId);
    }

    @Test
    void retornaListaVaziaSeNenhumaClasseTemRoleTeacher() {
        UUID userId = UUID.randomUUID();

        RequestContext context = new RequestContext(userId, TypeUser.TEACHER, List.of(
                new ContextClass(UUID.randomUUID(), ClassRole.STUDENT)
        ));
        when(contextProvider.getRequestContext()).thenReturn(context);

        List<UUID> result = adapter.getAccessibleClassIds(userId, TypeUser.TEACHER);

        assertThat(result).isEmpty();
    }

    @Test
    void retornaListaVaziaSeClassesVazia() {
        UUID userId = UUID.randomUUID();

        RequestContext context = new RequestContext(userId, TypeUser.TEACHER, List.of());
        when(contextProvider.getRequestContext()).thenReturn(context);

        List<UUID> result = adapter.getAccessibleClassIds(userId, TypeUser.TEACHER);

        assertThat(result).isEmpty();
    }
}
